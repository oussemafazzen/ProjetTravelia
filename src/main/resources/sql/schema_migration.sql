-- ============================================
-- Migration SQL : Fonctionnalités Avancées
-- Email Tokens, Face Recognition, Google Sign-In
-- ============================================

-- 1. Ajouter colonnes à la table client
ALTER TABLE client ADD COLUMN google_id VARCHAR(255) DEFAULT NULL;
ALTER TABLE client ADD COLUMN email_confirmed BOOLEAN DEFAULT FALSE;

-- 2. Table pour les tokens de réinitialisation mot de passe
CREATE TABLE IF NOT EXISTS password_reset_token (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES client(id) ON DELETE CASCADE
);

-- 3. Table pour les données de reconnaissance faciale
CREATE TABLE IF NOT EXISTS face_data (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    face_token VARCHAR(255) NOT NULL,
    face_encoding TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES client(id) ON DELETE CASCADE
);

-- 4. Table journal de sécurité
CREATE TABLE IF NOT EXISTS security_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT DEFAULT NULL,
    event_type VARCHAR(50) NOT NULL,
    ip_address VARCHAR(45),
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES client(id) ON DELETE SET NULL
);

-- Index pour performances
CREATE INDEX idx_token ON password_reset_token(token);
CREATE INDEX idx_face_user ON face_data(user_id);
CREATE INDEX idx_security_event ON security_log(event_type, created_at);
CREATE INDEX idx_google_id ON client(google_id);
