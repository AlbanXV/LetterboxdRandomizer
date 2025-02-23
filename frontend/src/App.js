
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
      alert(response.data);
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
    <div className='max-w-[1940px] ease-in-out text-white w-full h-screen mx-auto text-center'>
      <h1 className='md:text-5xl py-4'>Letterboxd Watchlist Randomizer</h1>
      <p className='md:text-2xl my-3'>Enter username:</p>
      <input className='text-black rounded-full py-2 px-3 mx-2' type="text" value={username} onChange={(e) => setUsername(e.target.value)}></input>
      <button className='bg-green-400 hover:bg-green-500 py-2 px-3 rounded-full' onClick={addUser}>Add user</button>
      {error && <p className='my-2 text-red-400'>{error}</p>}
      <br />
      <button className='bg-blue-400 hover:bg-blue-500 rounded-full py-2 px-3 my-3' onClick={getRandomMovie}>Get random movie</button>
      {movie && (
        <div className=' rounded-xl md:text-xl bg-linear-65 from-sky-500 to-indigo-500'>
          <p>{movie.title} ({movie.year})</p>
          <p>Director: {movie.director}</p>
          <p>Movie length: {movie.length} mins</p>
          <p>Link: <a className='text-blue-400 hover:text-blue-500' href={movie.link}>{movie.link}</a></p>
        </div>
      )}
    </div>
  );
}

export default App;
