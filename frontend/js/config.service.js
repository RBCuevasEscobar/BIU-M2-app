import Api from './api.js';

const ConfigService = (function () {

    let config = null;
    let loadingPromise = null;

    const DEFAULTS = {
        iva: 0.16,
        monedaSistema: 'MXN',
        maxProductosOrden: 999,
        stockMinimo: 3
    };

    async function load() {

        // Ya cargado → cache
        if (config) return config;

        // Ya se está cargando → reutilizar promesa
        if (loadingPromise) return loadingPromise;

        // Primera carga
        loadingPromise = Api.get('/config/sistema/publica')
            .then(cfg => {

                config = {
                    iva: Number(cfg.iva ?? DEFAULTS.iva),
                    monedaSistema: cfg.monedaSistema ?? DEFAULTS.monedaSistema,
                    maxProductosOrden: Number(cfg.maxProductosOrden ?? DEFAULTS.maxProductosOrden),
                    stockMinimo: Number(cfg.stockMinimo ?? DEFAULTS.stockMinimo)
                };

                return config;
            })
            .catch(err => {
                console.warn('[ConfigService] fallback defaults', err);
                config = { ...DEFAULTS };
                return config;
            });

        return loadingPromise;
    }

    function get() {
        return config || DEFAULTS;
    }

    function reset() {
        config = null;
        loadingPromise = null;
    }

    return {
        load,
        get,
        reset
    };

})();

export default ConfigService;