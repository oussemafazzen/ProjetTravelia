-- ============================================
-- Module 4 : Activités Touristiques
-- ============================================

CREATE TABLE IF NOT EXISTS `activite` (
  `id_activite` INT AUTO_INCREMENT PRIMARY KEY,
  `nom` VARCHAR(255) NOT NULL,
  `description` TEXT,
  `lieu` VARCHAR(255),
  `duree` INT,
  `prix` DOUBLE,
  `capacite_max` INT,
  `categorie` VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `inscriptionactivite` (
  `id_inscription` INT AUTO_INCREMENT PRIMARY KEY,
  `date_activite` DATE,
  `nombre_participants` INT,
  `statut` VARCHAR(50),
  `id_client` INT,
  `id_activite` INT,
  FOREIGN KEY (`id_activite`) REFERENCES `activite`(`id_activite`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
