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

/**
 * Fase 1 — Cidade Poluída.
 *
 * O jogador pedala (teclas ← / →) para gerar energia limpa.
 * Conforme pedala, o overlay de poluição diminui visualmente.
 * Ao atingir a meta de pedaladas, avança para a Fase 2.
 *
 * Sprites esperados (adicionar em lwjgl3/src/main/resources/):
 *   personagem_idle.png    — personagem parado (64×96)
 *   personagem_pedal.png   — personagem pedalando (spritesheet 4 frames, 256×96)
 *   arvore.png             — árvore que aparece conforme progresso (64×96)
 */
public class Fase1 implements Screen {

    // -----------------------------------------------------------------------
    // Constantes
    // -----------------------------------------------------------------------

    private static final float MUNDO_LARGURA   = 1280f;
    private static final float MUNDO_ALTURA    = 720f;

    /** Pedaladas necessárias para concluir a fase. */
    private static final int META_PEDALADAS = 60;

    /** Largura/Altura do placeholder do personagem. */
    private static final float CHAR_W = 64f;
    private static final float CHAR_H = 96f;

    // -----------------------------------------------------------------------
    // Dependências
    // -----------------------------------------------------------------------

    private final Main         jogo;
    private final SpriteBatch  batch;
    private final IoTSimulador iot;

    // -----------------------------------------------------------------------
    // Gráficos
    // -----------------------------------------------------------------------

    private Viewport   viewport;
    private OrthographicCamera camera;

    private Texture texFundo;
    private Texture texPersonagem;      // placeholder — substituir por sprite real
    private Texture texOverlay;         // overlay preto para simular poluição
    private Texture texBarraFundo;
    private Texture texBarraEnergia;
    private Texture texArvore;          // placeholder de árvore

    private BitmapFont fonte;

    // -----------------------------------------------------------------------
    // Estado da fase
    // -----------------------------------------------------------------------

    private float progresso   = 0f;     // 0.0 → 1.0
    private boolean pausado   = false;
    private boolean concluida = false;

    // Animação do personagem
    private float bobTimer  = 0f;
    private float bobOffset = 0f;

    // -----------------------------------------------------------------------
    // Construtor
    // -----------------------------------------------------------------------

    public Fase1(Main jogo) {
        this.jogo  = jogo;
        this.batch = new SpriteBatch();
        this.iot   = new IoTSimulador();
    }

    // -----------------------------------------------------------------------
    // Screen — ciclo de vida
    // -----------------------------------------------------------------------

    @Override
    public void show() {
        camera   = new OrthographicCamera();
        viewport = new FitViewport(MUNDO_LARGURA, MUNDO_ALTURA, camera);
        carregarRecursos();
    }

    @Override
    public void render(float delta) {
        if (!pausado) update(delta);
        draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause()  { pausado = true;  }
    @Override public void resume() { pausado = false; }
    @Override public void hide()   { Gdx.input.setInputProcessor(null); }

    @Override
    public void dispose() {
        batch.dispose();
        texFundo.dispose();
        texPersonagem.dispose();
        texOverlay.dispose();
        texBarraFundo.dispose();
        texBarraEnergia.dispose();
        texArvore.dispose();
        fonte.dispose();
    }

    // -----------------------------------------------------------------------
    // Update
    // -----------------------------------------------------------------------

    private void update(float delta) {
        iot.update(delta);

        progresso = Math.min(1f, (float) iot.getTotalPedaladas() / META_PEDALADAS);

        // Animação de bob proporcional ao RPM
        if (iot.isPedalando()) {
            float velocidadeBob = Math.max(2f, iot.getRpm() * 0.08f);
            bobTimer  += delta * velocidadeBob;
            bobOffset  = (float) Math.sin(bobTimer * Math.PI * 2) * 6f;
        } else {
            bobTimer  = 0f;
            bobOffset = 0f;
        }

        // ESC = pausar / despausar
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            pausado = !pausado;
        }

        // Fase concluída
        if (progresso >= 1f && !concluida) {
            concluida = true;
            // TODO: trocar por Fase2 quando implementada
            // jogo.setScreen(new Fase2(jogo));
        }
    }

    // -----------------------------------------------------------------------
    // Render
    // -----------------------------------------------------------------------

    private void draw() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // 1 — Fundo da cidade poluída
        batch.setColor(Color.WHITE);
        batch.draw(texFundo, 0, 0, MUNDO_LARGURA, MUNDO_ALTURA);

        // 2 — Overlay de poluição (opacidade decresce com progresso)
        float alphaOverlay = (1f - progresso) * 0.65f;
        batch.setColor(1f, 1f, 1f, alphaOverlay);
        batch.draw(texOverlay, 0, 0, MUNDO_LARGURA, MUNDO_ALTURA);
        batch.setColor(Color.WHITE);

        // 3 — Árvores surgem conforme progresso (máx 5 árvores)
        desenharArvores();

        // 4 — Personagem com animação de bob
        float charX = MUNDO_LARGURA * 0.12f;
        float charY = MUNDO_ALTURA  * 0.12f + bobOffset;
        batch.draw(texPersonagem, charX, charY, CHAR_W, CHAR_H);

        // 5 — HUD
        desenharBarraEnergia();
        desenharMetricas();

        // 6 — Tela de pausa
        if (pausado) desenharPausa();

        // 7 — Tela de conclusão
        if (concluida) desenharConclusao();

        batch.end();
    }

    private void desenharArvores() {
        int qtdArvores = (int) (progresso * 5);
        float[] posX = { 300f, 500f, 700f, 900f, 1100f };
        for (int i = 0; i < qtdArvores; i++) {
            batch.draw(texArvore, posX[i], MUNDO_ALTURA * 0.12f, 64f, 96f);
        }
    }

    private void desenharBarraEnergia() {
        float barX = 40f;
        float barY = 22f;
        float barW = 480f;
        float barH = 28f;
        float pad  = 4f;

        batch.draw(texBarraFundo,    barX - pad,  barY - pad,  barW + pad * 2, barH + pad * 2);
        batch.draw(texBarraEnergia,  barX,        barY,        barW * progresso, barH);

        fonte.getData().setScale(1.1f);
        fonte.draw(batch,
            "Energia: " + (int)(progresso * 100) + "%  —  Meta: " + META_PEDALADAS + " pedaladas",
            barX, barY + barH + 22f);
    }

    private void desenharMetricas() {
        float x = MUNDO_LARGURA - 270f;
        float y = MUNDO_ALTURA  - 20f;
        float esp = 26f;

        fonte.getData().setScale(1.1f);
        fonte.draw(batch, String.format("RPM:       %.0f",     iot.getRpm()),                x, y);
        fonte.draw(batch, String.format("Consist:   %.0f%%",   iot.getConsistencia() * 100), x, y - esp);
        fonte.draw(batch, String.format("Pedaladas: %d/%d",    iot.getTotalPedaladas(), META_PEDALADAS), x, y - esp * 2);
        fonte.draw(batch, String.format("Tempo:     %ds",      (int) iot.getTempoTotal()),   x, y - esp * 3);

        // Dica de teclas (canto superior esquerdo)
        fonte.draw(batch, "[←] Pedal Esq    [→] Pedal Dir    [ESC] Pausar",
            40f, MUNDO_ALTURA - 20f);
    }

    private void desenharPausa() {
        batch.setColor(0f, 0f, 0f, 0.6f);
        batch.draw(texOverlay, 0, 0, MUNDO_LARGURA, MUNDO_ALTURA);
        batch.setColor(Color.WHITE);

        fonte.getData().setScale(2.5f);
        fonte.draw(batch, "PAUSADO", MUNDO_LARGURA / 2f - 90f, MUNDO_ALTURA / 2f + 20f);
        fonte.getData().setScale(1.2f);
        fonte.draw(batch, "Pressione ESC para continuar", MUNDO_LARGURA / 2f - 140f, MUNDO_ALTURA / 2f - 30f);
    }

    private void desenharConclusao() {
        batch.setColor(0f, 0f, 0f, 0.5f);
        batch.draw(texOverlay, 0, 0, MUNDO_LARGURA, MUNDO_ALTURA);
        batch.setColor(Color.YELLOW);

        fonte.getData().setScale(2.8f);
        fonte.draw(batch, "FASE 1 CONCLUÍDA!", MUNDO_LARGURA / 2f - 200f, MUNDO_ALTURA / 2f + 30f);
        batch.setColor(Color.WHITE);
        fonte.getData().setScale(1.3f);
        fonte.draw(batch, "Ótimo trabalho! A cidade começa a se recuperar.",
            MUNDO_LARGURA / 2f - 220f, MUNDO_ALTURA / 2f - 30f);
        fonte.draw(batch, String.format("Pedaladas: %d  |  RPM médio: %.0f  |  Consistência: %.0f%%",
            iot.getTotalPedaladas(), iot.getRpm(), iot.getConsistencia() * 100),
            MUNDO_LARGURA / 2f - 220f, MUNDO_ALTURA / 2f - 70f);
    }

    // -----------------------------------------------------------------------
    // Recursos
    // -----------------------------------------------------------------------

    private void carregarRecursos() {
        texFundo = new Texture(Gdx.files.internal("bg_cidade_poluida.png"));
        texFundo.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        fonte = new BitmapFont(Gdx.files.internal("pixel_font.fnt"));
        fonte.getData().setScale(1.1f);
        fonte.getRegion().getTexture().setFilter(
            Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        // Placeholders — substituir quando os sprites estiverem prontos
        texPersonagem   = carregarOuPlaceholder("personagem_idle.png",  64,  96, new Color(0.3f, 0.7f, 0.3f, 1f));
        texArvore       = carregarOuPlaceholder("arvore.png",           64,  96, new Color(0.1f, 0.5f, 0.1f, 1f));
        texOverlay      = criarTexturaColorida(1, 1, Color.BLACK);
        texBarraFundo   = criarTexturaColorida(1, 1, new Color(0.15f, 0.15f, 0.15f, 1f));
        texBarraEnergia = criarTexturaColorida(1, 1, new Color(0.2f,  0.8f,  0.3f,  1f));
    }

    /**
     * Tenta carregar a textura pelo nome; se não existir, gera um placeholder colorido.
     * Quando o sprite estiver pronto, basta adicioná-lo em resources/ com o mesmo nome.
     */
    private Texture carregarOuPlaceholder(String nome, int w, int h, Color cor) {
        if (Gdx.files.internal(nome).exists()) {
            return new Texture(Gdx.files.internal(nome));
        }
        return criarTexturaColorida(w, h, cor);
    }

    private Texture criarTexturaColorida(int w, int h, Color cor) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(cor);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }
}
