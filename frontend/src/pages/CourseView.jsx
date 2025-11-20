import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import api from '../services/api';
import { useToggles } from '../context/ToggleContext';
import { useAuth } from '../context/AuthContext';

const CourseView = () => {
    const { id } = useParams();
    const { toggles } = useToggles();
    const { user } = useAuth();
    const [files, setFiles] = useState([]);
    const [comments, setComments] = useState([]);
    const [newComment, setNewComment] = useState('');
    const [uploadFile, setUploadFile] = useState(null);
    const [previewUrl, setPreviewUrl] = useState(null);
    const [selectedImage, setSelectedImage] = useState(null);
    const [message, setMessage] = useState('');

    useEffect(() => {
        fetchFiles();
        fetchComments();
    }, [id]);

    const fetchFiles = async () => {
        try {
            const response = await api.get(`/course/${id}/files`);
            setFiles(response.data);
        } catch (error) {
            console.error("Failed to fetch files", error);
        }
    };

    const fetchComments = async () => {
        try {
            const response = await api.get(`/course/${id}/comment`);
            setComments(response.data);
        } catch (error) {
            console.error("Failed to fetch comments", error);
        }
    };

    const handleFileChange = (e) => {
        const f = e.target.files[0];
        setUploadFile(f);
        if (f) setPreviewUrl(URL.createObjectURL(f));
    };

    const handleFileUpload = async (e) => {
        e.preventDefault();
        if (!uploadFile) return;

        const formData = new FormData();
        formData.append('file', uploadFile);
        formData.append('courseId', id);

        try {
            await api.post('/course/upload', formData, {
                headers: { 'Content-Type': 'multipart/form-data' }
            });
            setMessage('File uploaded successfully!');
            setUploadFile(null);
            setPreviewUrl(null);
            fetchFiles();
        } catch (error) {
            setMessage('Upload failed: ' + (error.response?.data?.error || error.message));
        }
    };

    const resetComments = async () => {
        await fetch(`/course/${id}/comments/reset`, { method: "DELETE", credentials: "include" });
        fetchComments();
    };

    const handleCommentSubmit = async (e) => {
        e.preventDefault();
        try {
            await api.post(`/course/${id}/comment`, {
                content: newComment,
                author: user.username
            });
            setNewComment('');
            fetchComments();
        } catch (error) {
            console.error("Failed to post comment", error);
        }
    };

    return (
        <div className="course-view-container">
            {message && <div className="alert">{message}</div>}

            <div className="section">
                <h3>Course Files</h3>
                <form onSubmit={handleFileUpload} className="upload-form">
                    <input type="file" accept=".jpg,.png,.pdf" onChange={handleFileChange} />
                    {previewUrl && (
                        <div className="mt-3" style={{ marginTop: '10px', marginBottom: '10px' }}>
                            {uploadFile.type.startsWith("image/") ? (
                                <img src={previewUrl} className="w-64 rounded shadow" style={{ maxWidth: '200px', borderRadius: '5px' }} />
                            ) : (
                                <div className="bg-gray-200 p-2 rounded" style={{ background: '#334155', padding: '5px', borderRadius: '5px' }}>PDF selected: {uploadFile.name}</div>
                            )}
                        </div>
                    )}
                    <button type="submit" className="btn-primary">Upload</button>
                </form>
                <div className="uploaded-images-grid">
                    {files.map(file => (
                        <div key={file.id} className="uploaded-image-card">
                            {file.contentType && file.contentType.startsWith('image/') ? (
                                <img
                                    src={`http://localhost:8080/course/files/${file.storedFilename}`}
                                    alt={file.originalFilename}
                                    className="uploaded-thumbnail"
                                    onClick={() => setSelectedImage(`http://localhost:8080/course/files/${file.storedFilename}`)}
                                    style={{ cursor: 'pointer' }}
                                />
                            ) : (
                                <a href={`http://localhost:8080/course/files/${file.storedFilename}`} target="_blank" rel="noopener noreferrer">
                                    {file.originalFilename}
                                </a>
                            )}
                            <div className="file-info">
                                <small>{file.originalFilename}</small>
                                <small className="file-meta">{(file.size / 1024).toFixed(2)} KB</small>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <div className="section">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h3>Comments</h3>
                    <button onClick={resetComments} className="bg-red-500 text-white px-3 py-1 rounded" style={{ background: '#ef4444', color: 'white', border: 'none', padding: '5px 10px', borderRadius: '5px', cursor: 'pointer' }}>Reset Comments</button>
                </div>
                <div className="comments-list">
                    {comments.map(comment => (
                        <div key={comment.id} className="comment-card">
                            <strong>{comment.author}</strong>
                            {/* 
                  XSS VULNERABILITY DEMO:
                  If xssProtection is OFF, the backend returns raw HTML.
                  To demonstrate XSS, we must render it as HTML.
                  If xssProtection is ON, the backend returns encoded HTML, so rendering it as HTML is safe (it will show the tags).
                  Wait, if backend encodes it (e.g. &lt;script&gt;), and we render it as HTML, it will show "<script>".
                  If backend does NOT encode it (e.g. <script>), and we render it as HTML, it will EXECUTE.
                  So we should ALWAYS render as HTML to demonstrate the difference.
              */}
                            <div dangerouslySetInnerHTML={{ __html: comment.content }} />
                            <small>{new Date(comment.createdAt).toLocaleString()}</small>
                        </div>
                    ))}
                </div>
                <form onSubmit={handleCommentSubmit} className="comment-form">
                    <textarea
                        value={newComment}
                        onChange={(e) => setNewComment(e.target.value)}
                        placeholder="Write a comment... (Try <img src=x onerror=alert(1)>)"
                    />
                    <button type="submit" className="btn-secondary">Post Comment</button>
                </form>
            </div>

            {selectedImage && (
                <div className="image-modal" onClick={() => setSelectedImage(null)}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <button className="modal-close" onClick={() => setSelectedImage(null)}>×</button>
                        <img src={selectedImage} alt="Preview" className="modal-image" />
                    </div>
                </div>
            )}
        </div>
    );
};

export default CourseView;
