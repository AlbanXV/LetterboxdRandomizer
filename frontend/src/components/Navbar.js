import React from "react";
import { Link } from 'react-router-dom';
import {navbar_items} from './navbar_items';

function Navbar() {
    return (
        <>
            <nav className="text-white">
                <div className="flex justify-between items-center h-24 mx-auto px-4 w-max">
                    <ul className="hidden md:flex">
                        {navbar_items.map((item, index) => {
                            return (
                                <li key={index} className="p-4">
                                    <Link to={item.url}>{item.title}</Link>
                                </li>
                            )
                        })}
                    </ul>
                </div>
            </nav>
        </>
    );
}

export default Navbar