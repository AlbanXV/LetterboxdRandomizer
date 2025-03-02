
import { useEffect, useState } from 'react';
import axios from 'axios';

function Customlistfetch() {
  const [movie, setMovie] = useState(null);
  const [url, setUrl] = useState('');
  const [error, setError] = useState('');

  const getRandomMovie = async () => {
    try {
      const response = await axios.post('http://localhost:8080/get_random_from_cList', null, { params: { url }, });
      if (response.data.error) {
        setError(response.data.error);
        setMovie(null);
      }
      else {
        setMovie(response.data);
        setError('');
      }
    } catch (err) {
      setError('Failed to fetch random movie from the list.');
    }
  };
  
  return (
    <div className='max-w-[1940px] text-white w-full h-screen mx-auto text-center justify-center items-center flex flex-col'>
      <h1 className='md:text-5xl py-4 font-bold'>Letterboxd Custom Link Randomizer</h1>
      <p className='md:text-2xl my-3'>Enter url of list:</p>
      <input className='text-black rounded-full py-2 px-3 mx-2 w-96' type="text" value={url} onChange={(e) => setUrl(e.target.value)}></input>
      <button className='bg-blue-400 hover:bg-blue-500 rounded-full py-2 px-3 my-3 ease-in-out duration-100' onClick={getRandomMovie}>Submit</button>
      {movie && (
        <div className=' rounded-xl md:text-xl'>
          <p>{movie.title} ({movie.year})</p>
          <p>Director: {movie.director}</p>
          <p>Movie length: {movie.length} mins</p>
          <p>Link: <a className='text-blue-400 hover:text-blue-500 ease-in-out duration-100' href={movie.link}>{movie.link}</a></p>
        </div>
      )}
    </div>
  );
}

export default Customlistfetch;
