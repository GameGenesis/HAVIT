import { BrowserRouter, Routes, Route } from "react-router-dom";

import Home from './pages/Home';
import Layout from "./pages/Layout";
import NoPage from './pages/NoPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout Component={Home} />} />
        <Route path="*" element={<Layout Component={NoPage} />} />
      </Routes>
    </BrowserRouter>
  );
}
