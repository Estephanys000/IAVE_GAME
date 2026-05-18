package br.mackenzie;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MenuPrincipal implements Screen {

    private static final float MUNDO_LARGURA = 1280f;
    private static final float MUNDO_ALTURA = 720f;
    private static final float BOTAO_LARGURA = 260f;
    private static final float BOTAO_ALTURA = 60f;
    private static final float BOTAO_ESPACO = 16f;
    private static final float TABELA_MARGEM_ESQ = 260f;
    private static final float TABELA_MARGEM_BOT = 60f;

    // CORREÇÃO: Tipado diretamente como 'Main' para evitar problemas de polimorfismo
    private final Main jogo;
    private final SpriteBatch batch;

    private Texture texFundo;
    private BitmapFont fonte;

    private Viewport viewport;
    private Stage stage;
    private Table tabela;

    private Array<TextButton> botoes;
    private int indiceSelecionado = 0;

    // Flag de segurança para evitar cliques múltiplos/concorrentes que quebram o OpenGL
    private boolean trocandoTela = false;

    public MenuPrincipal(Main jogo) {
        this.jogo = jogo;
        this.batch = jogo.batch;
    }

    @Override
    public void show() {
        trocandoTela = false;
        carregarRecursos();
        configurarStage();
        criarBotoes();
        configurarInput();
        atualizarFoco();
    }

    private void carregarRecursos() {
        texFundo = new Texture(Gdx.files.internal("bg_cidade_poluida.png"));
        texFundo.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        fonte = new BitmapFont(Gdx.files.internal("pixel_font.fnt"));
        fonte.getRegion().getTexture().setFilter(
                Texture.TextureFilter.Nearest,
                Texture.TextureFilter.Nearest
        );

        fonte.getData().setScale(1.5f);
    }

    private void configurarStage() {
        OrthographicCamera camera = new OrthographicCamera();
        viewport = new FitViewport(MUNDO_LARGURA, MUNDO_ALTURA, camera);
        stage = new Stage(viewport, batch);
    }

    private void criarBotoes() {
        botoes = new Array<>();

        TextButtonStyle estilo = new TextButtonStyle();
        estilo.font = fonte;
        estilo.fontColor = Color.WHITE;

        TextButton btnNovoJogo = new TextButton("Novo Jogo", estilo);
        TextButton btnLevels = new TextButton("Levels", estilo);
        TextButton btnSair = new TextButton("Sair", estilo);

        botoes.add(btnNovoJogo);
        botoes.add(btnLevels);
        botoes.add(btnSair);

        adicionarListeners(btnNovoJogo, 0);
        adicionarListeners(btnLevels, 1);
        adicionarListeners(btnSair, 2);

        tabela = new Table();
        tabela.setFillParent(true);
        tabela.bottom().left();

        tabela.pad(TABELA_MARGEM_BOT, TABELA_MARGEM_ESQ, TABELA_MARGEM_BOT, 0);

        tabela.add(btnNovoJogo).size(BOTAO_LARGURA, BOTAO_ALTURA).left().row();
        tabela.add(btnLevels).size(BOTAO_LARGURA, BOTAO_ALTURA).left().padTop(BOTAO_ESPACO).row();
        tabela.add(btnSair).size(BOTAO_LARGURA, BOTAO_ALTURA).left().padTop(BOTAO_ESPACO).row();

        stage.addActor(tabela);
    }

    private void adicionarListeners(TextButton botao, final int indice) {
        botao.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                if (pointer == -1 && !trocandoTela) {
                    indiceSelecionado = indice;
                    atualizarFoco();
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!trocandoTela) {
                    executarAcao(indice);
                }
            }
        });
    }

    private void atualizarFoco() {
        for (int i = 0; i < botoes.size; i++) {
            botoes.get(i)
                    .getLabel()
                    .setColor(i == indiceSelecionado ? Color.YELLOW : Color.WHITE);
        }
    }

    private void executarAcao(int indice) {
        if (trocandoTela) return;

        switch (indice) {
            case 0: // Novo Jogo (Vai direto para Fase1)
                if (jogo != null) {
                    trocandoTela = true;
                    Gdx.input.setInputProcessor(null);
                    jogo.setScreen(new Fase1(jogo));
                }
                break;

            case 1: // Levels (Agora vai para a tela Levels!)
                if (jogo != null) {
                    trocandoTela = true;
                    Gdx.input.setInputProcessor(null);
                    jogo.setScreen(new Levels(jogo)); // <--- AQUI ESTÁ A CORREÇÃO
                }
                break;

            case 2: // Sair
                Gdx.app.exit();
                break;
        }
    }

    private void configurarInput() {
        InputAdapter inputTeclado = criarInputTeclado();
        Gdx.input.setInputProcessor(new InputMultiplexer(stage, inputTeclado));
    }

    private InputAdapter criarInputTeclado() {
        return new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (trocandoTela) return false;

                switch (keycode) {
                    case Keys.DOWN:
                        if (indiceSelecionado < botoes.size - 1) {
                            indiceSelecionado++;
                            atualizarFoco();
                        }
                        return true;

                    case Keys.UP:
                        if (indiceSelecionado > 0) {
                            indiceSelecionado--;
                            atualizarFoco();
                        }
                        return true;

                    case Keys.ENTER:
                    case Keys.SPACE:
                        executarAcao(indiceSelecionado);
                        return true;
                }
                return false;
            }
        };
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        batch.draw(texFundo, 0, 0, MUNDO_LARGURA, MUNDO_ALTURA);
        batch.end();

        // Só atualiza o Stage se não estivermos no meio da transição
        if (!trocandoTela) {
            stage.act(delta);
            stage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        texFundo.dispose();
        fonte.dispose();
        stage.dispose();
    }
}