// Global Config for API. Update this for Azure Deployment
const API_BASE_URL = 'https://ecommerce-app-backend-g7esd5gpfnekadcx.centralus-01.azurewebsites.net/api';

// For ES Modules
export { API_BASE_URL };

// For legacy standard scripts (app.js, validation.js)
if (typeof window !== 'undefined') {
    window.API_BASE_URL = API_BASE_URL;
}
