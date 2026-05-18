package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Levels extends ScreenAdapter {

    private static final float MUNDO_LARGURA = 1280f;
    private static final float MUNDO_ALTURA = 720f;

    private final Main jogo;
    private SpriteBatch batch;
    private Texture telaLevels;
    private BitmapFont fonte;

    private OrthographicCamera camera;
    private Viewport viewport;

    private int opcaoSelecionada = 0;
    private float tempo = 0f;
    private boolean trocandoTela = false;

    public Levels(Main jogo) {
        this.jogo = jogo;
    }

    @Override
    public void show() {
        trocandoTela = false;
        batch = jogo.batch;

        camera = new OrthographicCamera();
        viewport = new FitViewport(MUNDO_LARGURA, MUNDO_ALTURA, camera);

        try {
            telaLevels = new Texture(Gdx.files.internal("levels.png"));
            telaLevels.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

            fonte = new BitmapFont(Gdx.files.internal("pixel_font.fnt"));
            fonte.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            fonte.getData().setScale(3f);
            fonte.setColor(Color.YELLOW);
        } catch (Exception e) {
            Gdx.app.error("ERRO_LEVELS", "Faltou a imagem 'levels.png' ou a fonte na pasta assets!");
            e.printStackTrace();
            Gdx.app.exit();
        }

        // CORREÇÃO: Passamos a usar InputAdapter igual ao menu.
        // Isso impede que o ENTER segurado transborde da tela anterior.
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (trocandoTela) return false;

                switch (keycode) {
                    case Input.Keys.RIGHT:
                        opcaoSelecionada++;
                        if (opcaoSelecionada > 2) opcaoSelecionada = 0;
                        return true;

                    case Input.Keys.LEFT:
                        opcaoSelecionada--;
                        if (opcaoSelecionada < 0) opcaoSelecionada = 2;
                        return true;

                    case Input.Keys.ENTER:
                    case Input.Keys.SPACE:
                        confirmarOpcao();
                        return true;

                    case Input.Keys.ESCAPE:
                        trocandoTela = true;
                        Gdx.input.setInputProcessor(null);
                        jogo.setScreen(new MenuPrincipal(jogo));
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void render(float delta) {
        tempo += delta;

        ScreenUtils.clear(Color.BLACK);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        if (!trocandoTela) {
            desenharTela();
        }
    }

    private void confirmarOpcao() {
        trocandoTela = true;
        Gdx.input.setInputProcessor(null);

        switch (opcaoSelecionada) {
            case 0: // Fácil
                jogo.setScreen(new Fase1(jogo));
                break;
            case 1: // Médio
                jogo.setScreen(new Fase2(jogo));
                break;
            case 2: // Difícil
                jogo.setScreen(new Fase3(jogo));
                break;
        }
    }

    private void desenharTela() {
        if (telaLevels == null) return;

        batch.begin();
        batch.draw(telaLevels, 0, 0, MUNDO_LARGURA, MUNDO_ALTURA);
        desenharIndicadorSelecao();
        batch.end();
    }

    private void desenharIndicadorSelecao() {
        float x;

        if (opcaoSelecionada == 0) {
            x = MUNDO_LARGURA * 0.24f;
        } else if (opcaoSelecionada == 1) {
            x = MUNDO_LARGURA * 0.50f;
        } else {
            x = MUNDO_LARGURA * 0.76f;
        }

        float y = MUNDO_ALTURA * 0.17f;
        float brilho = 0.85f + (float) Math.sin(tempo * 6f) * 0.15f;

        fonte.setColor(1f, brilho, 0f, 1f);
        fonte.draw(batch, "X", x, y);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        if (telaLevels != null) telaLevels.dispose();
        if (fonte != null) fonte.dispose();
    }
}