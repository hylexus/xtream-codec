import React from 'react';
import ReactDOM from 'react-dom/client';
import { App } from './app';
import { LayoutProvider } from './layout';
import './styles.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <LayoutProvider>
      <App />
    </LayoutProvider>
  </React.StrictMode>,
);
