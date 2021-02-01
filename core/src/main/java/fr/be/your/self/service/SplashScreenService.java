package fr.be.your.self.service;

import fr.be.your.self.model.SplashScreen;

public interface SplashScreenService extends BaseService<SplashScreen, Integer> {

	public SplashScreen getMainSplashScreen();
}
