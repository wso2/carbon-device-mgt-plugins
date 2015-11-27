
asset.configure = function () {
    return {
        table: {
            overview: {
                fields: {
                    thumbnail: {
                        type: 'file'
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
            //setupGuide_guideImage: 'setupGuide_guideImage'
        }
    };
};
