import { useState, useEffect, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCameraRotate,faMagnifyingGlass,faBackward,faSpinner,faCheck,faUpload,faFaceFrown,faTriangleExclamation } from '@fortawesome/free-solid-svg-icons'
import Navbar from "../components/Navbar";
import PhotoCard from "../components/PhotoCard";
import api from "../api/axios";
import "./GuestCollectionView.css";

export default function GuestCollectionView() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [collection, setCollection] = useState(null);
  const [photos, setPhotos] = useState([]);
  const [matchedPhotos, setMatchedPhotos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searching, setSearching] = useState(false);
  const [error, setError] = useState("");
  const [searchError, setSearchError] = useState("");
  const [searchDone, setSearchDone] = useState(false);
  const [selfiePreview, setSelfiePreview] = useState(null);
  const selfieRef = useRef(null);
  const fileInputRef = useRef(null);
  const [uploading, setUploading] = useState(false);

  useEffect(() => {
    fetchCollection();
    fetchPhotos();
  }, [id]);

  const fetchCollection = async () => {
    try {
      const res = await api.get(`/api/collections/${id}`);
      setCollection(res.data);
    } catch (err) {
      setError("Collection not found or has been deleted.");
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

  const handleSelfieChange = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setSelfiePreview(URL.createObjectURL(file));
    setSearchDone(false);
    setMatchedPhotos([]);
    setSearchError("");
  };

  const handleUpload = async (e) => {
  const files = Array.from(e.target.files);
  if (files.length === 0) return;

  setUploading(true);
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
    alert(err.response?.data?.message || "Upload failed.");
  } finally {
    setUploading(false);
    fileInputRef.current.value = "";
  }
};

  const handleSearch = async () => {
    const file = selfieRef.current.files[0];
    if (!file) {
      setSearchError("Please upload a selfie first.");
      return;
    }
    setSearching(true);
    setSearchError("");
    setSearchDone(false);

    const formData = new FormData();
    formData.append("selfie", file);

    try {
      const res = await api.post(
        `/api/collections/${id}/search`,
        formData,
        { headers: { "Content-Type": "multipart/form-data" } }
      );
      setMatchedPhotos(res.data.matchedPhotos);
      setSearchDone(true);
    } catch (err) {
      setSearchError(
        err.response?.data?.message || "Search failed. Try again."
      );
    } finally {
      setSearching(false);
    }
  };

  const handleClearSearch = () => {
    setMatchedPhotos([]);
    setSearchDone(false);
    setSelfiePreview(null);
    setSearchError("");
    selfieRef.current.value = "";
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
        <button
          className="btn-secondary"
          onClick={() => navigate("/collections/saved")}
        >
          ← Back
        </button>
      </div>
    </div>
  );

  return (
    <div className="page">
      <Navbar />
      <div className="container">

        {/* Header */}
        <div className="gcv-header">
          <div className="gcv-header-left">
            <button
              className="btn-secondary"
              onClick={() => navigate("/collections/saved")}
            >
                    <FontAwesomeIcon icon={faBackward} /> Back
            </button>
            <h2 className="page-title">{collection?.name}</h2>
          </div>
          <span className="gcv-owner">by {collection?.ownerName}</span>
        </div>

        {/* Selfie search box */}
        <div className="gcv-search-box">
          <h3 className="gcv-search-title"><span><FontAwesomeIcon icon={faMagnifyingGlass} /></span> Find My Photos</h3>
          <p className="gcv-search-desc">
            Upload a clear selfie to instantly find all photos
            where you appear in this collection.
          </p>

          <div className="gcv-search-row">
            {/* Selfie preview */}
            <div
              className="gcv-selfie-preview"
              onClick={() => selfieRef.current.click()}
            >
              {selfiePreview ? (
                <img
                  src={selfiePreview}
                  alt="selfie preview"
                  className="gcv-selfie-img"
                />
              ) : (
                <div className="gcv-selfie-placeholder">
                  <span><FontAwesomeIcon icon={faCameraRotate} /></span>
                  <span>Upload Selfie</span>
                </div>
              )}
            </div>

            {/* Search actions */}
            <div className="gcv-search-actions">
              <input
                type="file"
                ref={selfieRef}
                accept="image/*"
                style={{ display: "none" }}
                onChange={handleSelfieChange}
              />
              <button
                className="btn-secondary"
                onClick={() => selfieRef.current.click()}
              >
                Choose Selfie
              </button>
              <button
                className="btn-primary"
                onClick={handleSearch}
                disabled={searching || !selfiePreview}
              >
                {searching ? "Searching..." : "Search My Photos"}
              </button>
              {searchDone && (
                <button
                  className="btn-secondary"
                  onClick={handleClearSearch}
                >
                  Clear Search
                </button>
              )}
            </div>
          </div>

          {searchError && (
            <div className="error-msg gcv-search-error">
              {searchError}
            </div>
          )}

          {searching && (
            <div className="gcv-searching">
              <FontAwesomeIcon icon={faSpinner} spinPulse size="lg" style={{color: "#4f46e5",}} />
               Searching through photos... this may take some time.
            </div>
          )}

          {searchDone &&(
            <div className="success-msg">
              <FontAwesomeIcon icon={faCheck} />
               Found {matchedPhotos.length} photo(s) with you in collection!
            </div>
          )}
        </div>

{/* Upload section for guests */}
<div className="gcv-upload-box">
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
    {uploading ?  (<><FontAwesomeIcon icon={faSpinner} spinPulse/> Uploading</>) : (<><FontAwesomeIcon icon={faUpload} /> Upload Photos</>)}
  </button>
  <p className="gcv-upload-hint">
    You can upload photos to this collection. Max 20MB per photo.
  </p>
  <p className="gcv-upload-warning">
    <span><FontAwesomeIcon icon={faTriangleExclamation} /></span> 
    Wait 30-60 seconds after uploading before searching — face processing happens in background.
  </p>
</div>

        {/* Results or full gallery */}
        {searchDone ? (
          <>
            <h3 className="gcv-gallery-title">
              Your Photos ({matchedPhotos.length})
            </h3>
            {matchedPhotos.length === 0 ? (
              <div className="empty-state">
                <FontAwesomeIcon icon={faFaceFrown} />
                 No matching photos found. Try a clearer selfie
                or adjust the lighting.
              </div>
            ) : (
              <div className="photo-grid">
                {matchedPhotos.map((photo) => (
                  <PhotoCard
                    key={photo.id}
                    photo={photo}
                    showDelete={false}
                  />
                ))}
              </div>
            )}
          </>
        ) : (
          <>
            <h3 className="gcv-gallery-title">
              All Photos ({photos.length})
            </h3>
            {photos.length === 0 ? (
              <div className="empty-state">
                <FontAwesomeIcon icon={faFaceFrown} />
                No photos uploaded yet.
              </div>
            ) : (
              <div className="photo-grid">
                {photos.map((photo) => (
                  <PhotoCard
                    key={photo.id}
                    photo={photo}
                    showDelete={false}
                  />
                ))}
              </div>
            )}
          </>
        )}

      </div>
    </div>
  );
}