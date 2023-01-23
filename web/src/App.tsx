import { BrowserRouter, Routes, Route } from "react-router-dom";

import Home from './pages/Home';
import Layout from "./pages/Layout";
import NoPage from './pages/NoPage';
import Privacy from './pages/Privacy'
import Tech from "./pages/Tech";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Layout Component={Home} />} />
        <Route path="*" element={<Layout Component={NoPage} />} />
        <Route path="/privacy" element={<Layout Component={Privacy} />} />
        <Route path="/tech" element={<Layout Component={Tech} />} />
      </Routes>
    </BrowserRouter>
  );
}
