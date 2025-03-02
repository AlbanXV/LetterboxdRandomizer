
import { useEffect, useState } from 'react';
import axios from 'axios';

function Popularlist() {
  const [username, setUsername] = useState('');
  const [users, setUsers] = useState([]);
  const [movie, setMovie] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchUsers();
  }, []);

  const fetchUsers = async () => {
    try {
      const response = await axios.get('http://localhost:8080/get-users');
      setUsers(response.data);
    } catch (err) {
      setError('Failed to fetch users.');
    }
  };

  const addUser = async () => {
    try {
      const response = await axios.post('http://localhost:8080/add-user', null, { params: { username },});
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

  const deleteUser = async (user) => {
    try {
      await axios.delete('http://localhost:8080/delete-user', { params: { username: user }});
      fetchUsers();

    } catch (err) {
      setError('Failed to delete user.');
    }
  };
  
  return (
    <div className='max-w-[1940px] text-white w-full h-screen mx-auto text-center justify-center items-center flex flex-col'>
      <h1 className='md:text-5xl py-4 font-bold'>Letterboxd All-time popular Randomizer</h1>
      <button className='bg-blue-400 hover:bg-blue-500 rounded-full py-2 px-3 my-3 ease-in-out duration-100' onClick={getRandomMovie}>Get random movie</button>
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

export default Popularlist;
