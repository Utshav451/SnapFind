import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import ProtectedRoute from "./components/ProtectedRoute";
import "./styles/global.css";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import MyCollections from "./pages/MyCollections";
import CreateCollection from "./pages/CreateCollection";
import CollectionDetail from "./pages/CollectionDetail";
import SavedCollections from "./pages/SavedCollections";
import AccessCollection from "./pages/AccessCollection";
import GuestCollectionView from "./pages/GuestCollectionView";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public routes */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Protected routes */}
          <Route path="/home" element={
            <ProtectedRoute><Home /></ProtectedRoute>
          } />
          <Route path="/collections/mine" element={
            <ProtectedRoute><MyCollections /></ProtectedRoute>
          } />
          <Route path="/collections/new" element={
            <ProtectedRoute><CreateCollection /></ProtectedRoute>
          } />
          <Route path="/collections/:id" element={
            <ProtectedRoute><CollectionDetail /></ProtectedRoute>
          } />
          <Route path="/collections/saved" element={
            <ProtectedRoute><SavedCollections /></ProtectedRoute>
          } />
          <Route path="/collections/access" element={
            <ProtectedRoute><AccessCollection /></ProtectedRoute>
          } />
          <Route path="/collections/:id/view" element={
            <ProtectedRoute><GuestCollectionView /></ProtectedRoute>
          } />

          {/* Default redirect */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  </StrictMode>
);
