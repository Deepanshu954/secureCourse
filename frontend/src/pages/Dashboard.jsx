import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { Link } from 'react-router-dom';

const Dashboard = () => {
    const [courses, setCourses] = useState([]);

    useEffect(() => {
        fetchCourses();
    }, []);

    const fetchCourses = async () => {
        try {
            const response = await api.get('/course');
            setCourses(response.data);
        } catch (error) {
            console.error("Failed to fetch courses", error);
        }
    };

    return (
        <div className="dashboard-container">
            <h2>Available Courses</h2>
            <div className="course-grid">
                {courses.map(course => (
                    <div key={course.id} className="course-card">
                        <h3>{course.title}</h3>
                        <p>{course.description}</p>
                        <Link to={`/course/${course.id}`} className="btn-secondary">View Course</Link>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default Dashboard;
