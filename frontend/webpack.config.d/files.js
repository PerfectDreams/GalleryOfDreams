config.module.rules.push(
    {
        test: /\.(svg)$/i,
        type: 'asset/source'
    },
    {
        test: /\.(png)$/i,
        type: 'asset/inline'
    }
);