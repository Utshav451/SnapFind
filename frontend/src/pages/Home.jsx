import { useNavigate } from "react-router-dom";
import Navbar from "../components/Navbar";
import { useAuth } from "../context/AuthContext";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faFaceSmile } from '@fortawesome/free-solid-svg-icons'
import { faPhotoFilm } from '@fortawesome/free-solid-svg-icons'
import { faFolderPlus } from '@fortawesome/free-solid-svg-icons'
import "./Home.css";

export default function Home() {
  const { user } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="page">
      <Navbar />
      <div className="container">
        <div className="home-welcome">
          <h2 className="home-title">Welcome, {user?.name}! <FontAwesomeIcon icon={faFaceSmile} spin style={{color: "rgb(13, 139, 236)",}} /></h2>
          <p className="home-subtitle">
            What would you like to do today?
          </p>
        </div>

        <div className="home-cards">

          {/* Photographer option */}
          <div
            className="home-card"
            onClick={() => navigate("/collections/mine")}
          >
            <div className="home-card-icon"><FontAwesomeIcon icon={faFolderPlus} size="xl" style={{color: "rgb(13, 139, 236)",}} /></div>
            <h3 className="home-card-title">Create a Collection</h3>
            <p className="home-card-desc">
              Upload photos from your event and share them
              with guests using a unique invite key.
            </p>
            <button className="btn-primary home-card-btn">
              Go to My Collections
            </button>
          </div>

          {/* Guest option */}
          <div
            className="home-card"
            onClick={() => navigate("/collections/saved")}
          >
            <div className="home-card-icon"><FontAwesomeIcon icon={faPhotoFilm} size="lg" style={{color: "rgb(13, 139, 236)",}} /></div>
            <h3 className="home-card-title">Access a Collection</h3>
            <p className="home-card-desc">
              Have an invite key? Access the collection,
              upload your own photos, and find yourself
              in any photo using your selfie.
            </p>
            <button className="btn-primary home-card-btn">
              Go to Saved Collections
            </button>
          </div>

        </div>
      </div>
    </div>
  );
}