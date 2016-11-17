package com.example.newdemo;

public class ImageFacade {

    protected ProduceBinImage produceBinImage;
    protected ProduceBinHandImage produceBinHandImage;
    protected ProduceBinBackImage produceBinBackImage;

    public ImageFacade(AppContainer appContainer) {
        produceBinImage = new ProduceBinImage(appContainer);
        produceBinHandImage = new ProduceBinHandImage(appContainer.container);
        produceBinBackImage = new ProduceBinBackImage(appContainer.container);
    }
}
