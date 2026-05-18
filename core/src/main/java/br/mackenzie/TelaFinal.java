package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class TelaFinal implements Screen {

    private static final float W = 1280f;
    private static final float H = 720f;

    private final Main jogo;
    private final SpriteBatch batch;

    private final int totalPedaladas;
    private final float rpmMedio;
    private final float rpmMaximo;
    private final float consistencia;
    private final float tempoTotal;

    private final int arvores;
    private final float energia;
    private final float co2;

    private final int indiceLimpeza;
    private final String classificacao;
    private final Color cor;

    private Viewport viewport;
    private OrthographicCamera camera;

    private Texture fundo;
    private Texture painel;

    private BitmapFont font;

    private float timer = 0f;

    public TelaFinal(Main jogo,
                     int totalPedaladas,
                     float rpmMedio,
                     float rpmMaximo,
                     float consistencia,
                     float tempoTotal) {

        this.jogo = jogo;

        // usa batch do Main (correto LibGDX)
        this.batch = jogo.batch;

        this.totalPedaladas = totalPedaladas;
        this.rpmMedio = rpmMedio;
        this.rpmMaximo = rpmMaximo;
        this.consistencia = consistencia;
        this.tempoTotal = tempoTotal;

        // cálculos do jogo
        this.arvores = Math.max(1, (int)(totalPedaladas * 0.5f));
        this.energia = totalPedaladas * 12f;
        this.co2 = totalPedaladas * 1.2f;

        float score =
                (totalPedaladas * 0.6f) +
                        (consistencia * 50f) +
                        (rpmMedio * 0.2f);

        this.indiceLimpeza = Math.min(100, (int) score);

        // 🏆 classificação
        if (indiceLimpeza >= 90) {
            classificacao = "PLATINA";
            cor = new Color(0.6f, 0.9f, 1f, 1f);
        } else if (indiceLimpeza >= 75) {
            classificacao = "OURO";
            cor = Color.GOLD;
        } else if (indiceLimpeza >= 55) {
            classificacao = "PRATA";
            cor = Color.LIGHT_GRAY;
        } else {
            classificacao = "BRONZE";
            cor = new Color(0.8f, 0.5f, 0.2f, 1f);
        }
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(W, H, camera);

        fundo = criarTex(new Color(0.05f, 0.2f, 0.05f, 1f));
        painel = criarTex(new Color(0, 0, 0, 0.7f));

        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {

        timer += delta;

        ScreenUtils.clear(Color.BLACK);
        viewport.apply();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(fundo, 0, 0, W, H);

        batch.setColor(0, 0, 0, 0.75f);
        batch.draw(painel, 120, 80, W - 240, H - 160);
        batch.setColor(1, 1, 1, 1);

        // 🎮 título
        font.getData().setScale(2f);
        font.setColor(cor);
        font.draw(batch, "CIDADE SALVA!", 430, 650);

        // 📊 dados
        font.getData().setScale(1f);
        font.setColor(Color.WHITE);

        font.draw(batch, "Classificação: " + classificacao, 200, 520);
        font.draw(batch, "Índice de Limpeza: " + indiceLimpeza, 200, 490);
        font.draw(batch, "Pedaladas: " + totalPedaladas, 200, 460);
        font.draw(batch, "RPM Médio: " + rpmMedio, 200, 430);
        font.draw(batch, "RPM Máximo: " + rpmMaximo, 200, 400);
        font.draw(batch, "Consistência: " + (consistencia * 100) + "%", 200, 370);

        // impacto ambiental
        font.draw(batch, "Árvores: " + arvores, 650, 460);
        font.draw(batch, "Energia: " + energia + " Wh", 650, 430);
        font.draw(batch, "CO2 evitado: " + co2, 650, 400);

        // tempo
        font.draw(batch, "Tempo: " + tempoTotal + "s", 650, 370);

        // instrução
        font.draw(batch, "ENTER ou ESC para voltar", 420, 200);

        batch.end();

        if (Gdx.input.isKeyJustPressed(Keys.ENTER)
                || Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            jogo.setScreen(new MenuPrincipal(jogo));
        }
    }

    @Override public void resize(int w, int h) { viewport.update(w, h, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (fundo != null) fundo.dispose();
        if (painel != null) painel.dispose();
        if (font != null) font.dispose();
    }

    private Texture criarTex(Color c) {
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(c);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }
}