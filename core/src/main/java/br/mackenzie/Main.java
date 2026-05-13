package br.mackenzie;

import com.badlogic.gdx.Game;
<<<<<<< HEAD
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
=======
>>>>>>> bcc90f437795f5dba77441a8f1b600f42ce668e4

/** Ponto de entrada da aplicação. Gerencia a troca de telas. */
public class Main extends Game {

<<<<<<< HEAD
    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
=======
    @Override
    public void create() {
>>>>>>> bcc90f437795f5dba77441a8f1b600f42ce668e4
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
<<<<<<< HEAD
    public void dispose() {
        if (batch != null) batch.dispose();
    }
}
=======
    public void dispose() { }
}
>>>>>>> bcc90f437795f5dba77441a8f1b600f42ce668e4
