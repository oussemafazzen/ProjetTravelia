import os

file_path = r'c:\Users\Oussema\IdeaProjects\prositjava\GestionHebergement\src\main\resources\css\client-style.css'

css_to_append = """
/* ============================================
   GO VOYAGE STYLE - Reservations Page
   ============================================ */
.booking-hero {
    -fx-background-image: url("../images/plane_bg.png");
    -fx-background-size: cover;
    -fx-background-position: center;
    -fx-min-height: 500;
    -fx-background-radius: 25;
}

.booking-overlay {
    -fx-background-color: rgba(0, 0, 0, 0.2);
    -fx-background-radius: 25;
}

.booking-search-card {
    -fx-background-color: white;
    -fx-background-radius: 15;
    -fx-padding: 30;
    -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.25), 20, 0, 0, 10);
}

.booking-tab-btn {
    -fx-background-color: transparent;
    -fx-text-fill: #64748b;
    -fx-font-weight: bold;
    -fx-padding: 12 25;
    -fx-cursor: hand;
}

.booking-tab-active {
    -fx-background-color: #f8fafc;
    -fx-text-fill: #0156ff;
    -fx-border-color: transparent transparent #0156ff transparent;
    -fx-border-width: 0 0 3 0;
}

.booking-input-group {
    -fx-background-color: #f1f5f9;
    -fx-background-radius: 10;
    -fx-padding: 10 15;
    -fx-border-color: #e2e8f0;
    -fx-border-radius: 10;
}

.booking-label-small {
    -fx-font-size: 11px;
    -fx-font-weight: bold;
    -fx-text-fill: #64748b;
    -fx-text-transform: uppercase;
}

.btn-go-voyage {
    -fx-background-color: #4caf50; /* Green theme from Go Voyage */
    -fx-text-fill: white;
    -fx-font-weight: 900;
    -fx-font-size: 16px;
    -fx-background-radius: 12;
    -fx-padding: 15 40;
    -fx-effect: dropshadow(gaussian, rgba(76, 175, 80, 0.4), 10, 0, 0, 4);
    -fx-cursor: hand;
}

.btn-go-voyage:hover {
    -fx-background-color: #43a047;
    -fx-scale-x: 1.03;
    -fx-scale-y: 1.03;
}
"""

with open(file_path, 'a', encoding='utf-8') as f:
    f.write(css_to_append)

print("Appended booking styles successfully")
