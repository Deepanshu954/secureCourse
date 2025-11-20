import React, { createContext, useState, useEffect, useContext } from 'react';
import api from '../services/api';

const ToggleContext = createContext();

export const ToggleProvider = ({ children }) => {
    const [toggles, setToggles] = useState({
        sqlInjectionProtection: true,
        fileUploadSecurity: true,
        xssProtection: true
    });

    useEffect(() => {
        fetchToggles();
    }, []);

    const fetchToggles = async () => {
        try {
            const response = await api.get('/toggles');
            setToggles(response.data);
        } catch (error) {
            console.error("Failed to fetch toggles", error);
        }
    };

    const updateToggle = async (key, value) => {
        try {
            const response = await api.post('/toggles/update', { key, value });
            setToggles(response.data);
        } catch (error) {
            console.error("Failed to update toggle", error);
        }
    };

    return (
        <ToggleContext.Provider value={{ toggles, updateToggle }}>
            {children}
        </ToggleContext.Provider>
    );
};

export const useToggles = () => useContext(ToggleContext);
