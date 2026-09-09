import api from "../api/axios";
import "./PhotoCard.css";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDownload } from '@fortawesome/free-solid-svg-icons'
import { faTrashCan } from '@fortawesome/free-solid-svg-icons'

export default function PhotoCard({ photo, showDelete, onDelete }) {

  const handleDownload = async () => {
    try {
      const response = await api.get(photo.fileUrl, {
        responseType: "blob",
      });
      const url = window.URL.createObjectURL(new Blob([response.data]));
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", photo.originalName);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (err) {
      alert("Download failed");
    }
  };

  return (
    <div className="photo-card">
      <img
        src={`http://localhost:8080${photo.fileUrl}`}
        alt={photo.originalName}
        className="photo-card-img"
      />
      <div className="photo-card-footer">
        <span className="photo-card-name">{photo.originalName}</span>
        <div className="photo-card-actions">
          <button className="btn-primary" onClick={handleDownload}>
            <FontAwesomeIcon icon={faDownload} />
          </button>
          {showDelete && (
            <button className="btn-danger" onClick={() => onDelete(photo.id)}>
              <FontAwesomeIcon icon={faTrashCan} />
            </button>
          )}
        </div>
      </div>
    </div>
  );
}