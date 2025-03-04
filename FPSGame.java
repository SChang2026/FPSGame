/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.fpsgame;

/**
 *
 * @author SChang2026
 */
 import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class FPSGame extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
    private int playerX, playerY;
    private int playerHealth = 100;
    private final int PLAYER_SPEED = 4;
    private boolean up, down, left, right, shooting;
    private Timer timer;
    private ArrayList<Projectile> projectiles = new ArrayList<>();
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private Random rand = new Random();
    private int shootCooldown = 0;
    private int mouseX, mouseY;
    private int wave = 1;
    private boolean gameOver = false;
    public static final int SCREEN_WIDTH = 1920;
    public static final int SCREEN_HEIGHT = 1080;
    private boolean waveWipeUsed = false;
    private long lastHealTime = 0;
    private boolean fireBoostActive = false;
    private long lastFireBoostTime = 0;
    private final int FIRE_BOOST_DURATION = 5000; // 5 seconds (in milliseconds)
    private final int FIRE_BOOST_COOLDOWN = 30000; // 30 seconds cooldown
    private final int HEAL_COOLDOWN = 60000;
    private ArrayList<PowerUp> powerUps = new ArrayList<>();
    private boolean speedBoostActive = false;
    private long speedBoostStartTime = 0;
    private final int SPEED_BOOST_DURATION = 6000; // 6 seconds
    private int shield = 0; // Extra shield (not affected by medkit)
   

    
    
    public FPSGame() {
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        playerX = SCREEN_WIDTH / 2;
        playerY = SCREEN_HEIGHT / 2;
        timer = new Timer(10, this);
        timer.start();
        spawnEnemies();
    }

    private void spawnEnemies() {
    enemies.clear();
    int numEnemies = Math.min(5 + wave, 40); // Cap at 40 enemies at wave 40

    for (int i = 0; i < numEnemies; i++) {
        int x, y;
        boolean overlaps;

        // Ensure no overlapping enemies
        do {
            overlaps = false;
            int side = rand.nextInt(4);
            if (side == 0) {
                x = rand.nextInt(SCREEN_WIDTH);
                y = -50;
            } else if (side == 1) {
                x = rand.nextInt(SCREEN_WIDTH);
                y = SCREEN_HEIGHT + 50;
            } else if (side == 2) {
                x = -50;
                y = rand.nextInt(SCREEN_HEIGHT);
            } else {
                x = SCREEN_WIDTH + 50;
                y = rand.nextInt(SCREEN_HEIGHT);
            }

            for (Enemy e : enemies) {
                if (Math.hypot(e.x - x, e.y - y) < e.size * 1.5) { // Ensuring distance between enemies
                    overlaps = true;
                    break;
                }
            }
        } while (overlaps);

        String[] enemyTypes = {"Red", "Blue", "Yellow", "Purple", "Green"};
        String type = enemyTypes[rand.nextInt(enemyTypes.length)];
        double health = 3 + (wave * 0.1);
        double speed = 1.5;
        int size = 4;

        switch (type) {
            case "Red" -> { health = 4 + (wave * 0.1); speed = 4; size = 5; }
            case "Blue" -> { health = 9 + (wave * 0.1); speed = 4; size = 7; }
            case "Yellow" -> { health = 2 + (wave * 0.1); speed = 5; size = 4; }
            case "Purple" -> { health = 4 + (wave * 0.1); speed = 4; size = 5; }
            case "Green" -> { health = 4 + (wave * 0.1); speed = 4; size = 5; }
        }

        enemies.add(new Enemy(x, y, type, health, speed, size));
    }
    
     if (rand.nextInt(10) < 2) { // 20% chance to spawn a power-up
    int powerX = rand.nextInt(SCREEN_WIDTH - 50) + 25;
    int powerY = rand.nextInt(SCREEN_HEIGHT - 50) + 25;
    if (rand.nextBoolean()) {
        powerUps.add(new SpeedBoost(powerX, powerY));
    } else {
        powerUps.add(new ShieldBoost(powerX, powerY));
    }
}
   if (wave % 10 == 0) {
    enemies.add(new Enemy(SCREEN_WIDTH / 2, -100, "Boss", 20 + wave, 1.2, 10));

   }
}
    public void activateSpeedBoost() {
    if (!speedBoostActive) {
        speedBoostActive = true;
        speedBoostStartTime = System.currentTimeMillis();
    }
}
   

    public void addShield(int amount) {
        shield = Math.min(shield + amount, 100);
}   
    



    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> up = true;
            case KeyEvent.VK_S -> down = true;
            case KeyEvent.VK_A -> left = true;
            case KeyEvent.VK_D -> right = true;
            case KeyEvent.VK_SPACE -> {
                if (!waveWipeUsed) {
                    enemies.clear();
                    wave++;
                    spawnEnemies();
                    waveWipeUsed = true;
                }
            }
            case KeyEvent.VK_E -> {
                if (System.currentTimeMillis() - lastHealTime >= HEAL_COOLDOWN) {
                    playerHealth = Math.min(100, playerHealth + 50);
                    lastHealTime = System.currentTimeMillis();
                }
            }
            case KeyEvent.VK_Q -> {
    if (!fireBoostActive && System.currentTimeMillis() - lastFireBoostTime >= FIRE_BOOST_COOLDOWN) {
        fireBoostActive = true;
        lastFireBoostTime = System.currentTimeMillis();
    }
}

            
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", getWidth() / 2 - 150, getHeight() / 2);
            return;
        }
        

        g.setColor(Color.GREEN);
        g.fillOval(playerX, playerY, 20, 20);

        g.setColor(Color.WHITE);
        for (Projectile p : projectiles) {
        g.fillOval((int) p.x, (int) p.y, 5, 5);
        }
        
        for (PowerUp p : powerUps) {
        p.draw(g);
        }

        for (Enemy e : enemies) {
            g.setColor(e.color);
            g.fillOval(e.x - e.size / 2, e.y - e.size / 2, e.size, e.size);
        }

        g.setColor(Color.WHITE);
     

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Health: " + playerHealth, 20, 50);
        g.drawString("Shield: " + shield, 20, 90);
        g.drawString("Wave: " + wave, 20, 130);
        g.drawString("Wave Wipe (SPACE): " + (waveWipeUsed ? "0" : "1"), 20, 170);
        g.drawString("Heal Cooldown (E): " + Math.max(0, (HEAL_COOLDOWN - (System.currentTimeMillis() - lastHealTime)) / 1000) + "s", 20, 210);
        g.drawString("Fire Boost (Q): " +  Math.max(0, (FIRE_BOOST_COOLDOWN - (System.currentTimeMillis() - lastFireBoostTime)) / 1000) + "s", 20, 250);
        g.drawString("Speed Boost: " + (speedBoostActive ? "ACTIVE" : "Inactive"), 20, 290);


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        // Handle Speed Boost Expiration
    if (speedBoostActive && System.currentTimeMillis() - speedBoostStartTime >= SPEED_BOOST_DURATION) {
    speedBoostActive = false;
    }

    // Move Player (double speed if speed boost is active)
    int actualSpeed = speedBoostActive ? PLAYER_SPEED * 2 : PLAYER_SPEED;
    if (up && playerY > 0) playerY -= actualSpeed;
    if (down && playerY < SCREEN_HEIGHT - 20) playerY += actualSpeed;
    if (left && playerX > 0) playerX -= actualSpeed;
    if (right && playerX < SCREEN_WIDTH - 20) playerX += actualSpeed;

    Iterator<PowerUp> powerUpIterator = powerUps.iterator();
    while (powerUpIterator.hasNext()) {
        PowerUp powerUp = powerUpIterator.next();
        if (Math.hypot(powerUp.x - playerX, powerUp.y - playerY) < 25) {
            powerUp.applyEffect(this);
            powerUpIterator.remove();
        }
    }


        for (Enemy enemy : enemies) {
       enemy.chasePlayer(playerX, playerY, enemies);
          if (Math.hypot(enemy.x - playerX, enemy.y - playerY) < 15) {
    if (shield > 0) {
        shield -= 1; // Damage shield first
        if (shield < 0) shield = 0; // Prevent negative shield
    } else {
        playerHealth -= 1; // Damage health only if no shield is left
        if (playerHealth <= 0) {
            gameOver = true;
        }
    }
}

        }

        if (shooting && shootCooldown <= 0) {
            Projectile p = new Projectile(playerX, playerY);
            p.updateDirection(mouseX, mouseY);
            projectiles.add(p);
            shootCooldown = 10;
        }
        shootCooldown--;

        Iterator<Projectile> iterator = projectiles.iterator();
        while (iterator.hasNext()) {
            Projectile p = iterator.next();
            p.move();

            Iterator<Enemy> enemyIterator = enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
               if (p.intersects(enemy)) {
        if (fireBoostActive) {
            enemy.health = 0; // Instant kill when fire boost is active
        } else {
            enemy.health -= 1;
        }
            // Fire boost duration check
        if (fireBoostActive && System.currentTimeMillis() - lastFireBoostTime >= FIRE_BOOST_DURATION) {
            fireBoostActive = false; // Disable fire boost after 5 seconds
        }


                    iterator.remove();
                    if (enemy.health <= 0) {
                        enemyIterator.remove();
                    }
                    break;
                }
            }
        }

        if (enemies.isEmpty()) {
            wave++;
            spawnEnemies();
        }

        repaint();
    }



    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> up = false;
            case KeyEvent.VK_S -> down = false;
            case KeyEvent.VK_A -> left = false;
            case KeyEvent.VK_D -> right = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            shooting = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            shooting = false;
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("FPS Game");
        FPSGame game = new FPSGame();
        frame.add(game);
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

class Projectile {
    double x, y;
    double dx, dy;
    final double speed = 7.0; // Ensure speed is a double

    public Projectile(int x, int y) {
        this.x = x;
        this.y = y;
        dx = 0;
        dy = 0;
    }

    public void updateDirection(double targetX, double targetY) {
        double angle = Math.atan2(targetY - y, targetX - x);
        dx = speed * Math.cos(angle);
        dy = speed * Math.sin(angle);
    }

    public void move() {
        x += dx;
        y += dy;
    }

    public boolean intersects(Enemy e) {
        return (Math.hypot(x - e.x, y - e.y) < e.size / 1.85);
    }
}


class Enemy {
    int x, y;
    double health;
    int size;
    double speed;
    Color color;

    public Enemy(int x, int y, String type, double health, double speed, int size) {
        this.x = x;
        this.y = y;
        this.health = health;
        this.speed = speed;
        this.size = size * 10;
        this.color = switch (type) {
            case "Red" -> Color.RED;
            case "Blue" -> Color.BLUE;
            case "Yellow" -> Color.YELLOW;
            case "Purple" -> new Color(128, 0, 128);
            case "Brown" -> new Color(139, 69, 19);
            case "Green" -> Color.GREEN;
            default -> Color.GRAY;
        };
    }
    

  public void chasePlayer(int playerX, int playerY, ArrayList<Enemy> enemies) {
    double angle = Math.atan2(playerY - y, playerX - x);
    int newX = x + (int) (speed * Math.cos(angle));
    int newY = y + (int) (speed * Math.sin(angle));

    // Adjust direction if too close to another enemy
    for (Enemy other : enemies) {
        if (other != this && Math.hypot(other.x - newX, other.y - newY) < size) {
            double avoidAngle = Math.atan2(y - other.y, x - other.x);
            newX += (int) (speed * Math.cos(avoidAngle) * 0.8); // Slightly move away
            newY += (int) (speed * Math.sin(avoidAngle) * 0.8);
        }
    }

    // Update enemy position with adjusted coordinates
    x = newX;
    y = newY;
}}
    abstract class PowerUp {
        int x, y;
        int size = 20;
        Color color;

        public PowerUp(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public abstract void applyEffect(FPSGame game);

        public void draw(Graphics g) {
            g.setColor(color);
            g.fillOval(x, y, size, size);
        }
    }
    
    class SpeedBoost extends PowerUp {
        public SpeedBoost(int x, int y) {
            super(x, y, Color.YELLOW);
        }

        @Override
        public void applyEffect(FPSGame game) {
            game.activateSpeedBoost();
        }
    }
    class ShieldBoost extends PowerUp {
        public ShieldBoost(int x, int y) {
            super(x, y, Color.BLUE);
        }

        @Override
        public void applyEffect(FPSGame game) {
            game.addShield(50);
        }
    }