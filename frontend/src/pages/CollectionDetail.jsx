import { useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faTriangleExclamation,faUpload,faBackward,faCheck,faCopy,faSadTear, faFaceSadTear } from '@fortawesome/free-solid-svg-icons'
import Navbar from "../components/Navbar";
import PhotoCard from "../components/PhotoCard";
import api from "../api/axios";
import "./CollectionDetail.css";

export default function CollectionDetail() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [collection, setCollection] = useState(null);
  const [photos, setPhotos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState("");
  const [copied, setCopied] = useState(false);

  const fileInputRef = useRef(null);

  useEffect(() => {
    fetchCollection();
    fetchPhotos();
  }, [id]);

  const fetchCollection = async () => {
    try {
      const res = await api.get(`/api/collections/${id}`);
      setCollection(res.data);
    } catch (err) {
      setError("Collection not found.");
    } finally {
      setLoading(false);
    }
  };

  const fetchPhotos = async () => {
    try {
      const res = await api.get(`/api/collections/${id}/photos`);
      setPhotos(res.data);
    } catch (err) {
      console.error("Failed to load photos");
    }
  };

  const handleUpload = async (e) => {
    const files = Array.from(e.target.files);
    if (files.length === 0) return;

    setUploading(true);
    setError("");

    const formData = new FormData();
    files.forEach((file) => formData.append("files", file));

    try {
      const res = await api.post(
        `/api/collections/${id}/photos`,
        formData,
        { headers: { "Content-Type": "multipart/form-data" } }
      );
      setPhotos((prev) => [...prev, ...res.data]);
    } catch (err) {
      setError(err.response?.data?.message || "Upload failed.");
    } finally {
      setUploading(false);
      fileInputRef.current.value = "";
    }
  };

  const handleDeletePhoto = async (photoId) => {
    // if (!window.confirm("Delete this photo?")) return;
    try {
      await api.delete(`/api/photos/${photoId}`);
      setPhotos(photos.filter((p) => p.id !== photoId));
    } catch (err) {
      alert("Failed to delete photo.");
    }
  };

  const handleCopyKey = async () => {
    const textToCopy = collection?.uniqueKey;
    if (!textToCopy) return;

    let successful = false;

    // Try modern Clipboard API (available in HTTPS or localhost)
    if (navigator.clipboard && window.isSecureContext) {
      try {
        await navigator.clipboard.writeText(textToCopy);
        successful = true;
      } catch (err) {
        console.warn("navigator.clipboard failed, attempting fallback...", err);
      }
    }

    // Fallback for HTTP contexts (e.g. EC2 public IP without SSL)
    if (!successful) {
      try {
        const textArea = document.createElement("textarea");
        textArea.value = textToCopy;
        textArea.style.position = "fixed";
        textArea.style.left = "-999999px";
        textArea.style.top = "-999999px";
        textArea.setAttribute("readonly", "");
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();
        successful = document.execCommand("copy");
        document.body.removeChild(textArea);
      } catch (err) {
        console.error("Fallback clipboard copy failed:", err);
      }
    }

    if (successful) {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  if (loading) return (
    <div className="page">
      <Navbar />
      <div className="loading">Loading collection...</div>
    </div>
  );

  if (error) return (
    <div className="page">
      <Navbar />
      <div className="container">
        <div className="error-msg">{error}</div>
      </div>
    </div>
  );

  return (
    <div className="page">
      <Navbar />
      <div className="container">

        {/* Header */}
        <div className="cd-header">
          <div className="cd-header-left">
            <button
              className="btn-secondary cd-back"
              onClick={() => navigate("/collections/mine")}
            >
                  <FontAwesomeIcon icon={faBackward} /> Back
            </button>
            <h2 className="page-title">{collection?.name}</h2>
          </div>
        </div>

        {/* Invite key box */}
        <div className="cd-key-box">
          <div className="cd-key-info">
            <span className="cd-key-label">Invite Key</span>
            <span className="cd-key-value">{collection?.uniqueKey}</span>
          </div>
          <button
            className={`btn-primary cd-copy-btn ${copied ? "cd-copied-btn" : ""}`}
            onClick={handleCopyKey}
          >
            {copied ? (
              <>
                <FontAwesomeIcon icon={faCheck} /> Copied
              </>
            ) : (
              <>
                <FontAwesomeIcon icon={faCopy} /> Copy
              </>
            )}
          </button>
        </div>

        {/* Upload section */}
        <div className="cd-upload-box">
          <input
            type="file"
            ref={fileInputRef}
            multiple
            accept="image/*"
            style={{ display: "none" }}
            onChange={handleUpload}
          />
          <button
            className="btn-primary"
            onClick={() => fileInputRef.current.click()}
            disabled={uploading}
          >
            {uploading ? "Uploading..." : <span><FontAwesomeIcon icon={faUpload} />Upload Photos</span>}
          </button>
          <p className="cd-upload-hint">
            You can upload multiple photos at once. Max 20MB per photo.
          </p>
          <p className="cd-upload-warning">
    <span><FontAwesomeIcon icon={faTriangleExclamation} /></span> Please wait 30-60 seconds after uploading before
    sharing the invite key. Face recognition processing
    happens in the background.
  </p>
        </div>

        {/* Photos gallery */}
        <div className="cd-gallery-header">
          <h3 className="cd-gallery-title">
            Photos ({photos.length})
          </h3>
        </div>

        {photos.length === 0 ? (
          <div className="empty-state">
              <FontAwesomeIcon icon={faFaceSadTear} size="2xl" bounce style={{color: "rgb(9, 116, 199)",}} />
             <h3>No photos yet. Upload some photos to get started!</h3>
          </div>
        ) : (
          <div className="photo-grid">
            {photos.map((photo) => (
              <PhotoCard
                key={photo.id}
                photo={photo}
                showDelete={true}
                onDelete={handleDeletePhoto}
              />
            ))}
          </div>
        )}

      </div>
    </div>
  );
}