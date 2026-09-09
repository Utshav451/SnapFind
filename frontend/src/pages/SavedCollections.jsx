import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faFolderOpen,faCalendarPlus,faBackward,faEye,faTrashCan } from '@fortawesome/free-solid-svg-icons'
import Navbar from "../components/Navbar";
import api from "../api/axios";
import "./SavedCollections.css";

export default function SavedCollections() {
  const [collections, setCollections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    fetchSaved();
  }, []);

  const fetchSaved = async () => {
    try {
      const res = await api.get("/api/collections/saved");
      setCollections(res.data);
    } catch (err) {
      setError("Failed to load saved collections.");
    } finally {
      setLoading(false);
    }
  };

  const handleRemove = async (id) => {
    // if (!window.confirm(
    //   "Remove this collection from your saved list?"
    // )) return;

    try {
      await api.delete(`/api/collections/saved/${id}`);
      setCollections(collections.filter((c) => c.id !== id));
    } catch (err) {
      alert("Failed to remove collection.");
    }
  };

  return (
    <div className="page">
      <Navbar />
      <div className="container">
        <div className="sc-header">
  <div className="sc-header-left">
    <button
      className="btn-secondary"
      onClick={() => navigate(-1)}
    >
            <FontAwesomeIcon icon={faBackward} /> Back
    </button>
    <h2 className="page-title">Saved Collections</h2>
  </div>
  <button
    className="btn-primary"
    onClick={() => navigate("/collections/access")}
  >
    <span><FontAwesomeIcon icon={faCalendarPlus} /></span> Access New Collection
  </button>
</div>

        {error && <div className="error-msg">{error}</div>}

        {loading ? (
          <div className="loading">Loading collections...</div>
        ) : collections.length === 0 ? (
          <div className="empty-state">
            <p><span><FontAwesomeIcon icon={faFolderOpen} style={{color: "rgb(239, 189, 20)",}} /></span> No saved collections yet.</p>
            <p>Use an invite key to access a collection!</p>
          </div>
        ) : (
          <div className="sc-grid">
            {collections.map((c) => (
              <div key={c.id} className="sc-card">
                <div className="sc-card-top">
                  <h3 className="sc-card-name">{c.name}</h3>
                  <span className="sc-card-owner">
                    by {c.ownerName}
                  </span>
                </div>
                <p className="sc-card-date">
                  Created: {new Date(c.createdAt).toLocaleDateString()}
                </p>
                <div className="sc-card-actions">
                  <button
                    className="btn-primary"
                    onClick={() => navigate(`/collections/${c.id}/view`)}
                  >
                    <FontAwesomeIcon icon={faEye} />
                    Photos
                  </button>
                  <button
                    className="btn-danger"
                    onClick={() => handleRemove(c.id)}
                  >
                    <FontAwesomeIcon icon={faTrashCan} />
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}