import React from 'react';
import { useToggles } from '../context/ToggleContext';

const SecurityTogglePanel = () => {
    const { toggles, updateToggle } = useToggles();

    return (
        <div className="security-panel">
            <h3>Security Controls</h3>
            <div className="toggle-item">
                <label>
                    <input
                        type="checkbox"
                        checked={toggles.sqlInjectionProtection}
                        onChange={(e) => updateToggle('sqlInjectionProtection', e.target.checked)}
                    />
                    SQL Injection Protection
                </label>
                <small>Protects Login</small>
            </div>
            <div className="toggle-item">
                <label>
                    <input
                        type="checkbox"
                        checked={toggles.fileUploadSecurity}
                        onChange={(e) => updateToggle('fileUploadSecurity', e.target.checked)}
                    />
                    File Upload Security
                </label>
                <small>Protects Uploads</small>
            </div>
            <div className="toggle-item">
                <label>
                    <input
                        type="checkbox"
                        checked={toggles.xssProtection}
                        onChange={(e) => updateToggle('xssProtection', e.target.checked)}
                    />
                    XSS Protection
                </label>
                <small>Protects Comments</small>
            </div>
        </div>
    );
};

export default SecurityTogglePanel;
