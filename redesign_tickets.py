import os

file_path = r'c:\Users\Oussema\IdeaProjects\prositjava\GestionHebergement\src\main\resources\views\Dashboard.fxml'

with open(file_path, 'r', encoding='utf-8') as f:
    content = f.read()

# Locate the ticketsView block
import re
pattern = r'(<VBox fx:id="ticketsView".*?</VBox>)'
# Use regex with DOTALL to match across multiple lines
match = re.search(pattern, content, re.DOTALL)

if match:
    old_block = match.group(1)
    new_block = """<VBox fx:id="ticketsView" alignment="TOP_CENTER" spacing="0.0" style="-fx-background-color: transparent;" visible="false">
                     <children>
                        <StackPane styleClass="booking-hero">
                           <children>
                              <Region styleClass="booking-overlay" />
                              <VBox alignment="CENTER" spacing="30.0">
                                 <children>
                                    <VBox alignment="CENTER" spacing="5.0">
                                       <children>
                                          <Label style="-fx-font-size: 32px; -fx-font-weight: 900; -fx-text-fill: white;" text="Où souhaitez-vous aller ?" />
                                          <Label style="-fx-font-size: 16px; -fx-text-fill: white;" text="Trouvez les meilleures offres de billets de transport" />
                                       </children>
                                    </VBox>

                                    <VBox maxWidth="1000.0" styleClass="booking-search-card">
                                       <children>
                                          <HBox spacing="10.0">
                                             <children>
                                                <Button styleClass="booking-tab-btn, booking-tab-active" text="✈ Vols" />
                                                <Button styleClass="booking-tab-btn" text="🏨 Hôtels" />
                                                <Button styleClass="booking-tab-btn" text="🚗 Voitures" />
                                             </children>
                                             <VBox.margin><Insets bottom="10.0" /></VBox.margin>
                                          </HBox>
                                          
                                          <HBox alignment="CENTER" spacing="15.0">
                                             <children>
                                                <VBox styleClass="booking-input-group" HBox.hgrow="ALWAYS">
                                                   <children>
                                                      <Label styleClass="booking-label-small" text="DEPUIS" />
                                                      <TextField promptText="Ville ou aéroport" style="-fx-background-color: transparent; -fx-padding: 5 0;" text="Tunis (TUN)" />
                                                   </children>
                                                </VBox>
                                                
                                                <Button minWidth="40.0" style="-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 20; -fx-background-radius: 20; -fx-text-fill: #1e293b;" text="⇌" />
                                                
                                                <VBox styleClass="booking-input-group" HBox.hgrow="ALWAYS">
                                                   <children>
                                                      <Label styleClass="booking-label-small" text="VERS" />
                                                      <TextField promptText="Ville ou aéroport" style="-fx-background-color: transparent; -fx-padding: 5 0;" />
                                                   </children>
                                                </VBox>
                                                
                                                <VBox styleClass="booking-input-group" HBox.hgrow="ALWAYS">
                                                   <children>
                                                      <Label styleClass="booking-label-small" text="DATE" />
                                                      <DatePicker promptText="Ajouter" style="-fx-background-color: transparent; -fx-border-color: transparent;" />
                                                   </children>
                                                </VBox>
                                                
                                                <Button mnemonicParsing="false" onAction="#showTickets" styleClass="btn-go-voyage" text="Ajouter un billet" />
                                             </children>
                                          </HBox>
                                       </children>
                                    </VBox>
                                 </children>
                                 <padding><Insets bottom="60.0" left="20.0" right="20.0" top="60.0" /></padding>
                              </VBox>
                           </children>
                           <VBox.margin><Insets bottom="20.0" left="20.0" right="20.0" top="10.0" /></VBox.margin>
                        </StackPane>
                        
                        <VBox alignment="CENTER" spacing="20.0">
                            <children>
                                <Label style="-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b;" text="Nos meilleures suggestions pour vous" />
                                <Label text="Découvrez des destinations incroyables au meilleur prix avec Travelia" textFill="#64748b" />
                            </children>
                            <padding><Insets bottom="40.0" left="40.0" right="40.0" top="30.0" /></padding>
                        </VBox>
                     </children>
                  </VBox>"""
    
    new_content = content.replace(old_block, new_block)
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(new_content)
    print("Redesigned ticketsView successfully")
else:
    print("ticketsView block not found")
