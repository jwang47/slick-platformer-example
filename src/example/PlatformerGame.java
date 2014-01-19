package example;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.KeyListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.tiled.TiledMap;

/**
 * Simple platformer game example using Box2D
 * 
 * Possible improvements:
 * 1. Stop using new everywhere
 * 2. Use the marching squares algorithm to generate edges
 *    rather than boxes for the platforms: http://en.wikipedia.org/wiki/Marching_squares
 * 3. Separate behavior into various classes
 */
public class PlatformerGame extends BasicGame implements ContactListener, KeyListener {

  /**
   * Pixels to meters ratio for Box2D. Useful because Box2D works best with
   * units in the 0.1 to 10.0 range. Adjust this accordingly.
   */
  private static final float PTM = 10.0f;
  private float timeStep = 1.0f / 60.0f;
  private float timeStepMillis = timeStep * 1000.0f;
  private int velocityIterations = 6;
  private int positionIterations = 2;
  private float timeMultiplier = 5;
  private float timeAccumulator = 0;
  private World world;

  private Vector2 cameraPosition;

  // player stuff
  public static final float JUMP_FORCE = 23.0f;
  private boolean canJump = true;
  private final float playerRadius = 5;
  private Vector2 playerPosition = new Vector2();
  private Vector2 playerSpawn;
  private Body playerBody;

  private static final int MAP_X = 0;
  private static final int MAP_Y = 0;
  private TiledMap map;

  public PlatformerGame() {
    super("platformer");
  }

  @Override
  public void render(GameContainer container, Graphics g) throws SlickException {
    Vec2 temp = playerBody.getPosition();
    playerPosition.set(temp.x, temp.y).mult(PTM);

    cameraPosition.set(playerPosition.getX() - container.getWidth() / 2, playerPosition.getY() - container.getHeight() / 2);
    // translate everything by the negative of the camera
    // this makes the camera work!
    g.translate(-cameraPosition.getX(), -cameraPosition.getY());

    // render stuff here
    map.render(MAP_X, MAP_Y);
    g.setColor(Color.orange);
    g.drawRect(playerPosition.getX() - playerRadius, playerPosition.getY() - playerRadius, playerRadius * 2, playerRadius * 2);
    // g.drawOval(playerPosition.getX() - playerRadius, playerPosition.getY() -
    // playerRadius, playerRadius * 2, playerRadius * 2);

    g.translate(cameraPosition.getX(), cameraPosition.getY());
  }

  @Override
  public void init(GameContainer g) throws SlickException {
	g.getInput().addListener(this);
	  
    world = new World(new Vec2(0, 9.81f), true);
    world.setContactListener(this);

    cameraPosition = new Vector2(0, 0);
    map = new TiledMap("res/map.tmx", "res");

    // now figure out where the player should spawn
    for (int groupID = 0; groupID < map.getObjectGroupCount(); groupID++) {
      for (int objectID = 0; objectID < map.getObjectCount(groupID); objectID++) {
        String type = map.getObjectType(groupID, objectID);
        String name = map.getObjectName(groupID, objectID);
        int x = map.getObjectX(groupID, objectID);
        int y = map.getObjectY(groupID, objectID);
        if (type.equals("spawn") && name.equals("player")) {
          // we found the object that we defined in map.tmx
          // convert to world-coordinates
          playerSpawn = tileMapToWorld(x, y);
          break;
        }
      }
    }

    // now generate box2d bodies for all "grass2" tiles
    int layerIndex = map.getLayerIndex("base");
    for (int tileX = 0; tileX < map.getWidth(); tileX++) {
      for (int tileY = 0; tileY < map.getHeight(); tileY++) {
        int tileID = map.getTileId(tileX, tileY, layerIndex);
        String type = map.getTileProperty(tileID, "type", null);
        if (type != null && type.equals("grass2")) {
          // create box2d body for this tile
          Vector2 position = tileToWorld(tileX, tileY);
          position.add(map.getTileWidth() / 2, map.getTileHeight() / 2);

          Vec2 physicsPosition = toPhysicsVector(position);
          createBoxBody(physicsPosition, map.getTileWidth(), map.getTileHeight(), BodyType.STATIC);
        }
      }
    }

    // create the player
    // playerSpawn.set(0, 0);
    System.out.println("position" + playerSpawn);
    playerBody = createCircleBody(toPhysicsVector(playerSpawn), playerRadius, BodyType.DYNAMIC);
    // set a string as the user data
    // usually set the object representing the body as the user data, to use when processing collisions
    playerBody.setUserData("player");
  }

  @Override
  public void update(GameContainer g, int delta) throws SlickException {
    // update box2d world
    timeAccumulator += delta * timeMultiplier;
    while (timeAccumulator > timeStepMillis) {
      world.step(timeStep, velocityIterations, positionIterations);
      timeAccumulator -= timeStepMillis;
    }

    // move player
    float seconds = delta / 1000.0f;
    float force = seconds * 2500;
    if (Keyboard.isKeyDown(Input.KEY_LEFT) || Keyboard.isKeyDown(Input.KEY_A)) {
      playerBody.applyForce(new Vec2(-force, 0), new Vec2());
    }
    if (Keyboard.isKeyDown(Input.KEY_RIGHT) || Keyboard.isKeyDown(Input.KEY_D)) {
      playerBody.applyForce(new Vec2(force, 0), new Vec2());
    }

  }

  /**
   * Converts a tile to world coordinates (top left corner of tile)
   */
  private Vector2 tileToWorld(int tileX, int tileY) {
    return new Vector2(MAP_X + map.getTileWidth() * tileX, MAP_Y + map.getTileHeight() * tileY);
  }

  /**
   * Converts from TiledMap coordinate system to world coordinate system
   */
  private Vector2 tileMapToWorld(float x, float y) {
    return new Vector2(MAP_X + x, MAP_Y + y);
  }

  /**
   * @param vector
   *          in pixel units
   * @return a Vec2 in meter units (Box2D units)
   */
  private Vec2 toPhysicsVector(Vector2 vector) {
    return new Vec2(vector.getX() / PTM, vector.getY() / PTM);
  }

  private Body createBoxBody(Vec2 position, float width, float height, BodyType type) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.position = position;
    bodyDef.type = type;
    bodyDef.linearDamping = 0.5f;

    FixtureDef fixtureDef = new FixtureDef();
    PolygonShape boxShape = new PolygonShape();
    boxShape.setAsBox(width * 0.5f / PTM, height * 0.5f / PTM);
    fixtureDef.shape = boxShape;

    Body body = world.createBody(bodyDef);
    body.createFixture(fixtureDef);
    return body;
  }

  private Body createCircleBody(Vec2 position, float radius, BodyType type) {
    BodyDef bodyDef = new BodyDef();
    bodyDef.position = position;
    bodyDef.type = type;
    bodyDef.linearDamping = 0.5f;

    FixtureDef fixtureDef = new FixtureDef();
    CircleShape circleShape = new CircleShape();
    circleShape.m_radius = radius / PTM;
    fixtureDef.shape = circleShape;

    Body body = world.createBody(bodyDef);
    body.createFixture(fixtureDef);
    return body;
  }

  private void processPlayerCollision(Contact contact, Body other) {
    // if we hit something and we weren't moving up, we can jump
    if (contact.getManifold().localNormal.y < 0 && contact.getManifold().localPoint.x == 0) {
      canJump = true;
    }
  }

  @Override
  public void beginContact(Contact contact) {
    Fixture fixtureA = contact.getFixtureA();
    Fixture fixtureB = contact.getFixtureB();
    Object dataA = fixtureA.getBody().getUserData();
    Object dataB = fixtureB.getBody().getUserData();
    if (dataA != null && dataA.equals("player")) {
      processPlayerCollision(contact, fixtureA.getBody());
    } else if (dataB != null && dataB.equals("player")) {
      processPlayerCollision(contact, fixtureB.getBody());
    }
  }

  @Override
  public void endContact(Contact contact) {

  }

  @Override
  public void preSolve(Contact contact, Manifold oldManifold) {

  }

  @Override
  public void postSolve(Contact contact, ContactImpulse impulse) {

  }
  
  @Override
  public void keyPressed(int key, char c) {
    // jump player
    if (canJump && (key == Input.KEY_UP || key == Input.KEY_W)) {
      playerBody.applyLinearImpulse(new Vec2(0, -JUMP_FORCE), new Vec2());
      canJump = false;
    }
  }
  
  @Override
  public void keyReleased(int key, char c) {
	  
  }

  public static void main(String[] argv) {
    try {
      System.out.println(Display.getAdapter());
      AppGameContainer container = new AppGameContainer(new PlatformerGame());
      container.setDisplayMode(800, 600, false);
      container.start();
    } catch (SlickException e) {
      e.printStackTrace();
    }
  }

}
