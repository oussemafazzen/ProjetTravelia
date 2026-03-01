-- =========================================================
-- SOLUTION DEFINITIVE : Structure password_reset_token
-- =========================================================

-- Vos captures d'écran montrent que la table actuelle utilise 'email' 
-- au lieu de 'user_id' et qu'elle ne possède pas la colonne 'used'.

USE GestionUtilisateur;

-- 1. On supprime la table incorrecte (Attention : Cette action vide les tokens en cours)
DROP TABLE IF EXISTS password_reset_token;

-- 2. On recrée la table avec EXACTEMENT la structure attendue par le code Java
CREATE TABLE password_reset_token (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES client(id) ON DELETE CASCADE
);

-- Note : J'utilise 'user_id' pour la cohérence avec votre table 'face_data'
-- qui possède déjà cette colonne (vu dans vos captures).
