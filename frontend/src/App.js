import './App.css';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from './components/pages/Home';
import Popular from './components/pages/Popular';
import CustomList from './components/pages/CustomList';
import Navbar from './components/Navbar';

function App() {
  return (
    <>
      <Router>
        <Navbar />
        <Routes>
          <Route path='/' exact element={<Home />} />
          <Route path='/popular' element={<Popular />} />
          <Route path='/customlist' element={<CustomList />} />
        </Routes>
      </Router>
    </>
  );
}

export default App;
