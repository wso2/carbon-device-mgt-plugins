asset.configure = function () {
    return {
        table: {
            overview: {
                fields: {
                    thumbnail: {
                        type: 'file'
                    },
                    name: {
                        placeholder: "Device Name Here..."
                    }
                }
            },
            setupGuide: {
                fields: {
                    guideImage: {
                        type: 'file'
                    }
                }
            }
        },
        meta: {
            lifecycle: {
                name: 'DeviceLifeCycle',
                defaultAction: 'Create',
                defaultLifecycleEnabled: true
            },
            thumbnail: 'overview_thumbnail',
            banner: 'overview_thumbnail'
        }
    };
};