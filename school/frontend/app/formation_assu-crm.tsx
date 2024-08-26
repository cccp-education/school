'use client';

import React from 'react';

const FormationProductFile: React.FC = () => {
  logHomeLoaded();
  return (
    <div
      style={{
        backgroundColor: '#2b2b2b',
        color: '#c5c8c6',
        minHeight: '100vh',
      }}
    >
      <header
        id="header"
        style={{
          backgroundColor: '#007bff',
          color: 'white',
          textAlign: 'center',
          padding: '1em 0',
        }}
      >
        <h1>Talaria - Organisme Formateur</h1>
      </header>

      <section id="catalogue" style={{ padding: '2em 0' }}>
        <div className="container">
          <h2 className="text-center mb-4">Catalogue des Formations</h2>

          <div
            className="formation-container"
            style={{
              display: 'flex',
              flexWrap: 'wrap',
              justifyContent: 'center',
            }}
          >
            {/* Formation Assu-CRM */}
            <div
              className="formation-card"
              style={{
                flexBasis: '300px',
                margin: '1em',
                border: '1px solid #ddd',
                borderRadius: '5px',
                overflow: 'hidden',
                transition: 'transform 0.3s',
              }}
            >
              <img
                src="https://cheroliv.github.io/img/assu-crm/Assu-CRM-training-img.png"
                alt="Formation Assu-CRM"
                style={{ width: '100%', height: '200px', objectFit: 'cover' }}
              />
              <div className="formation-card-body" style={{ padding: '1em' }}>
                <h3 className="card-title">Formation Assu-CRM</h3>
                <p className="card-text">
                  Guide détaillé pour utiliser l'outils des courtiers Assu-CRM.
                </p>
              </div>
            </div>
            {/*{logTrainingDisplayed('Formation Assu-CRM affichée')} */}

            {/* Formation Informatisation Assu */}
            <div
              className="formation-card"
              style={{
                flexBasis: '300px',
                margin: '1em',
                border: '1px solid #ddd',
                borderRadius: '5px',
                overflow: 'hidden',
                transition: 'transform 0.3s',
              }}
            >
              <img
                src="https://cheroliv.github.io/img/assu-crm/informatisation-assu-training-img.png"
                alt="Formation Informatisation Assu"
                style={{ width: '100%', height: '200px', objectFit: 'cover' }}
              />
              <div className="formation-card-body" style={{ padding: '1em' }}>
                <h3 className="card-title">Formation Informatisation Assu</h3>
                <p className="card-text">
                  Guide détaillé pour appréhender l'agilité et la communication collaborative.
                </p>
              </div>
            </div>
            {/* {logTrainingDisplayed('Formation Informatisation Assu affichée')} */}
          </div>
        </div>
      </section>
      {/* {logTrainingsDisplayed()} */}
      <Footer />
    </div>
  );
};
const Footer: React.FC = () => {
  console.log('Pied de page affiché');

  return (
    <footer
      style={{
        backgroundColor: '#4285f4',
        color: 'white',
        padding: '2em 0',
        textAlign: 'center',
      }}
    >
      <p>&copy; 2024 Talaria - Organisme Formateur. Tous droits réservés.</p>
    </footer>
  );
};
const logHomeLoaded = () => console.log("Page d'accueil chargée");

//const logTrainingDisplayed = (formation: string) => { console.log(formation) }

//const logTrainingsDisplayed = () => { console.log(`Formations affichées`) }

export default FormationProductFile;
