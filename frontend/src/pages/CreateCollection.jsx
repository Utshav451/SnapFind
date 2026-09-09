import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import api from "../api/axios";
import "./CreateCollection.css";

export default function CreateCollection() {
  const [name, setName] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleCreate = async () => {
    if (!name.trim()) {
      setError("Please enter a collection name.");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const res = await api.post("/api/collections", { name });
      //Go directly to collection detail page after creation
      navigate(`/collections/${res.data.id}`);
    } catch (err) {
      setError(err.response?.data?.message || "Failed to create collection.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <Navbar />
      <div className="container">
        <div className="cc-wrapper">
          <div className="card cc-card">

            <h2 className="page-title">Create New Collection</h2>
            <p className="cc-desc">
              Give your collection a name — you'll get a unique
              invite key to share with your guests.
            </p>

            {error && <div className="error-msg">{error}</div>}

            <div className="field">
              <label className="label">Collection Name</label>
              <input
                className="input"
                type="text"
                placeholder="e.g. Sara Wedding 2025"
                value={name}
                onChange={(e) => setName(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleCreate()}
              />
            </div>

            <div className="cc-actions">
              <button
                className="btn-secondary"
                onClick={() => navigate("/collections/mine")}
              >
                Cancel
              </button>
              <button
                className="btn-primary"
                onClick={handleCreate}
                disabled={loading}
              >
                {loading ? "Creating..." : "Create Collection"}
              </button>
            </div>

          </div>
        </div>
      </div>
    </div>
  );
}