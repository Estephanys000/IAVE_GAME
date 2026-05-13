package br.mackenzie;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** Ponto de entrada da aplicação. Gerencia a troca de telas. */
public class Main extends Game {

    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        this.setScreen(new MenuPrincipal(this));
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        super.resize(width, height);
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
    }
}