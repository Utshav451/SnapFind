from fastapi import FastAPI, File, UploadFile, HTTPException
from deepface import DeepFace
import tempfile
import os
from PIL import Image
import io
import numpy as np
from contextlib import asynccontextmanager

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("Loading face recognition models...")
    try:
        dummy = np.zeros((100, 100, 3), dtype=np.uint8)
        dummy_path = "dummy_warmup.jpg"
        Image.fromarray(dummy).save(dummy_path)
        DeepFace.represent(
            img_path=dummy_path,
            model_name="Facenet512",
            enforce_detection=False,
            detector_backend="retinaface"
        )
        os.remove(dummy_path)
        print("Models loaded successfully!")
    except Exception as e:
        print(f"Warmup error (ignored): {e}")
    yield

app = FastAPI(lifespan=lifespan)

@app.get("/health")
def health():
    return {"status": "running"}

@app.post("/extract")
async def extract_embedding(file: UploadFile = File(...)):
    tmp_path = None
    try:
        contents = await file.read()
        try:
            img = Image.open(io.BytesIO(contents))
            img.verify()
        except Exception:
            raise HTTPException(
                status_code=400, detail="Invalid image file")

        suffix = os.path.splitext(file.filename)[-1] or ".jpg"
        with tempfile.NamedTemporaryFile(
                delete=False, suffix=suffix) as tmp:
            tmp.write(contents)
            tmp_path = tmp.name

        result = DeepFace.represent(
            img_path=tmp_path,
            model_name="Facenet512",
            enforce_detection=False,
            detector_backend="retinaface"
        )

        if not result:
            return {"embeddings": [], "face_count": 0}

        embeddings = [r["embedding"] for r in result]
        return {"embeddings": embeddings, "face_count": len(embeddings)}

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(
            status_code=500, detail=f"Extraction failed: {str(e)}")
    finally:
        if tmp_path and os.path.exists(tmp_path):
            os.remove(tmp_path)