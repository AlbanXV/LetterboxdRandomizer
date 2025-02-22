
import { useState } from 'react';
import './App.css';
import axios from 'axios';

function App() {
  const [username, setUsername] = useState('');
  const [movie, setMovie] = useState(null);
  const [error, setError] = useState('');

  const addUser = async () => {
    try {
      const response = await axios.post('http://localhost:8080/add-user', null, {params: { username },});
      alert(response.data)
    } catch (err) {
      setError('Failed to add user.');
    }
  };

  const getRandomMovie = async () => {
    try {
      const response = await axios.get('http://localhost:8080/randomizer');
      if (response.data.error) {
        setError(response.data.error);
        setMovie(null);
      }
      else {
        setMovie(response.data);
        setError('');
      }
    } catch (err) {
      setError('Failed to fetch random movie.');
    }
  };
  
  return (
    <div>
      <h1>Letterboxd Watchlist Randomizer</h1>
      <p>Enter username:</p>
      <input type="text" value={username} onChange={(e) => setUsername(e.target.value)}></input>
      <button onClick={addUser}>Add user</button>
      <br />
      <br />
      <button onClick={getRandomMovie}>Get random movie</button>
      {error && <p>{error}</p>}
      {movie && (
        <div>
          <p>{movie.title}</p>
        </div>
      )}
    </div>
  );
}

export default App;
