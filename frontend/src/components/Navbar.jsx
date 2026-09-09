import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import "./Navbar.css";

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const isActive = (path) => location.pathname === path;

  return (
    <nav className="navbar">
      <span className="navbar-logo" onClick={() => navigate("/home")}>
        SnapFind
      </span>

      <div className="navbar-right">
        <span
          className={`navbar-link ${isActive("/home") ? "active" : ""}`}
          onClick={() => navigate("/home")}
        >
          Home
        </span>
        <span
          className={`navbar-link ${isActive("/collections/mine") ? "active" : ""}`}
          onClick={() => navigate("/collections/mine")}
        >
          My Collections
        </span>
        <span
          className={`navbar-link ${isActive("/collections/saved") ? "active" : ""}`}
          onClick={() => navigate("/collections/saved")}
        >
          Saved Collections
        </span>

        <span className="navbar-divider">|</span>

        <span className="navbar-user"> {user?.name}</span>
        <button className="navbar-logout" onClick={handleLogout}>
          Logout
        </button>
      </div>
    </nav>
  );
}