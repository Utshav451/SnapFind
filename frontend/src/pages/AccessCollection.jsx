import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import api from "../api/axios";
import "./AccessCollection.css";

export default function AccessCollection() {
  const [key, setKey] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleAccess = async () => {
    if (!key.trim()) {
      setError("Please enter an invite key.");
      return;
    }

    setLoading(true);
    setError("");

    try {
      const res = await api.post("/api/collections/access", { key });
      navigate(`/collections/${res.data.id}/view`);
    } catch (err) {
      setError(err.response?.data?.message || "Invalid invite key.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page">
      <Navbar />
      <div className="container">
        <div className="ac-wrapper">
          <div className="card ac-card">
            <h2 className="page-title">Access a Collection</h2>
            <p className="ac-desc">
              Enter the invite key shared by the photographer
              to access their photo collection.
            </p>

            {error && <div className="error-msg">{error}</div>}

            <div className="field">
              <label className="label">Invite Key</label>
              <input
                className="input ac-input"
                type="text"
                placeholder="e.g. SUNSET-4821"
                value={key}
                onChange={(e) => setKey(e.target.value.toUpperCase())}
                onKeyDown={(e) => e.key === "Enter" && handleAccess()}
              />
            </div>

            <div className="ac-actions">
              <button
                className="btn-secondary"
                onClick={() => navigate("/collections/saved")}
              >
                Cancel
              </button>
              <button
                className="btn-primary"
                onClick={handleAccess}
                disabled={loading}
              >
                {loading ? "Accessing..." : "Access Collection"}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}