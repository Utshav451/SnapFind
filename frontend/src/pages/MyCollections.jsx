import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCirclePlus,faBackward,faKey,faFolderOpen,faArrowRightToBracket,faTrashArrowUp } from '@fortawesome/free-solid-svg-icons'
import Navbar from "../components/Navbar";
import api from "../api/axios";
import "./MyCollections.css";

export default function MyCollections() {
  const [collections, setCollections] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    fetchCollections();
  }, []);

  const fetchCollections = async () => {
    try {
      const res = await api.get("/api/collections/mine");
      setCollections(res.data);
    } catch (err) {
      setError("Failed to load collections.");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id) => {
    // if (!window.confirm(
    //   "Delete this collection? All photos and the invite key will be permanently deleted."
    // )) return;

    try {
      await api.delete(`/api/collections/${id}`);
      setCollections(collections.filter((c) => c.id !== id));
    } catch (err) {
      alert("Failed to delete collection.");
    }
  };

  return (
    <div className="page">
      <Navbar />
      <div className="container">
    <div className="mc-header">
  <div className="mc-header-left">
    <button
      className="btn-secondary"
      onClick={() => navigate(-1)}
    >
      <FontAwesomeIcon icon={faBackward} /> Back
    </button>
    <h2 className="page-title">My Collections</h2>
  </div>
  <button
    className="btn-primary"
    onClick={() => navigate("/collections/new")}
  >
    <span><FontAwesomeIcon icon={faCirclePlus} /></span> New Collection
  </button>
</div>

        {error && <div className="error-msg">{error}</div>}

        {loading ? (
          <div className="loading">Loading collections...</div>
        ) : collections.length === 0 ? (
          <div className="empty-state">
            <p><span><FontAwesomeIcon icon={faFolderOpen} style={{color: "rgb(239, 189, 20)",}} /></span> No collections yet.</p>
            <p>Create your first collection to get started!</p>
          </div>
        ) : (
          <div className="mc-grid">
            {collections.map((c) => (
              <div key={c.id} className="mc-card">
                <div className="mc-card-top">
                  <h3 className="mc-card-name">{c.name}</h3>
                  <span className="mc-card-key"><FontAwesomeIcon icon={faKey} /> {c.uniqueKey}</span>
                </div>
                <p className="mc-card-date">
                  Created: {new Date(c.createdAt).toLocaleDateString()}
                </p>
                <div className="mc-card-actions">
                  <button
                    className="btn-primary"
                    onClick={() => navigate(`/collections/${c.id}`)}
                  >
                    <FontAwesomeIcon icon={faArrowRightToBracket} />
                    Open
                  </button>
                  <button
                    className="btn-danger"
                    onClick={() => handleDelete(c.id)}
                  >
                    <FontAwesomeIcon icon={faTrashArrowUp} />
                    Delete
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