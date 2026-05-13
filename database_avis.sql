CREATE TABLE avis (
    id_avis INT AUTO_INCREMENT PRIMARY KEY,
    commentaire TEXT NOT NULL,
    note INT NOT NULL CHECK (note >= 1 AND note <= 5),
    date_publication DATETIME DEFAULT CURRENT_TIMESTAMP,
    type_service VARCHAR(50) NOT NULL, -- 'hebergement', 'activite', 'transport'
    id_service INT NOT NULL,
    id_client INT NOT NULL,
    FOREIGN KEY (id_client) REFERENCES utilisateurs(id) ON DELETE CASCADE
);

CREATE TABLE photo_avis (
    id_photo INT AUTO_INCREMENT PRIMARY KEY,
    chemin_fichier VARCHAR(255) NOT NULL,
    legende VARCHAR(255),
    id_avis INT NOT NULL,
    FOREIGN KEY (id_avis) REFERENCES avis(id_avis) ON DELETE CASCADE
);
