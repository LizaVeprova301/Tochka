import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


public class run2 {
    // Константы для символов ключей и дверей
    private static final char[] KEYS_CHAR = new char[26];
    private static final char[] DOORS_CHAR = new char[26];


    static {
        for (int i = 0; i < 26; i++) {
            KEYS_CHAR[i] = (char) ('a' + i);
            DOORS_CHAR[i] = (char) ('A' + i);
        }
    }


    // Чтение данных из стандартного ввода
    private static char[][] getInput() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        List<String> lines = new ArrayList<>();
        String line;


        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            lines.add(line);
        }


        char[][] maze = new char[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            maze[i] = lines.get(i).toCharArray();
        }


        return maze;
    }

    static class Point {
        int x, y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    static class Step {
        Point point;
        char key;
        int steps;

        Step(Point point, char key, int steps) {
            this.point = point;
            this.key = key;
            this.steps = steps;
        }


    }

    static class State {
        int[][] positions;
        int keys;

        State(int[][] positions, int keys) {
            this.positions = positions;
            this.keys = keys;
        }

        String encode() {
            StringBuilder sb = new StringBuilder();
            for (int[] pos : positions) {
                sb.append(pos[0]).append(',').append(pos[1]).append(';');
            }
            sb.append('#').append(keys);
            return sb.toString();
        }
    }

    private static int[][] deepCopy(int[][] arr) {
        int[][] res = new int[arr.length][2];
        for (int i = 0; i < arr.length; i++) {
            res[i][0] = arr[i][0];
            res[i][1] = arr[i][1];
        }
        return res;
    }


    private static int solve(char[][] maze) {
        int rows = maze.length;
        int cols = maze[0].length;
        int allKeysMask = 0;
        List<Point> starts = new ArrayList<>();
        Map<Character, Point> keyPositions = new HashMap<>();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char c = maze[i][j];
                if (c == '@') {
                    starts.add(new Point(i, j));
                } else if (c >= 'a' && c <= 'z') {
                    allKeysMask |= (1 << (c - 'a'));
                    keyPositions.put(c, new Point(i, j));
                }
            }
        }

        int[][] positions = new int[starts.size()][2];
        for (int i = 0; i < starts.size(); i++) {
            positions[i][0] = starts.get(i).x;
            positions[i][1] = starts.get(i).y;
        }

        State startState = new State(positions, 0);
        int bound = heuristic(positions, 0, keyPositions);

        while (true) {
            Result res = idaSearch(maze, startState, 0, bound, allKeysMask, keyPositions, new HashSet<>());
            if (res.found) return res.cost;
            if (res.nextBound == Integer.MAX_VALUE) return Integer.MAX_VALUE;
            bound = res.nextBound;
        }
    }

    private static class Result {
        boolean found;
        int cost;
        int nextBound;

        Result(boolean found, int cost, int nextBound) {
            this.found = found;
            this.cost = cost;
            this.nextBound = nextBound;
        }
    }

    private static Result idaSearch(char[][] maze, State state, int g, int bound, int allKeysMask,
                                    Map<Character, Point> keyPositions, Set<String> visited) {
        int f = g + heuristic(state.positions, state.keys, keyPositions);
        if (f > bound) return new Result(false, 0, f);
        if (state.keys == allKeysMask) return new Result(true, g, 0);

        String code = state.encode();
        if (visited.contains(code)) return new Result(false, 0, Integer.MAX_VALUE);
        visited.add(code);

        int minBound = Integer.MAX_VALUE;

        for (int robot = 0; robot < state.positions.length; robot++) {
            List<Step> reachable = getReachableKeys(maze, state.positions[robot], state.keys);
            for (Step step : reachable) {
                int newKeys = state.keys | (1 << (step.key - 'a'));
                int[][] newPositions = deepCopy(state.positions);
                newPositions[robot][0] = step.point.x;
                newPositions[robot][1] = step.point.y;

                State nextState = new State(newPositions, newKeys);
                Result res = idaSearch(maze, nextState, g + step.steps, bound, allKeysMask, keyPositions, visited);
                if (res.found) return res;
                if (res.nextBound < minBound) minBound = res.nextBound;
            }
        }

        visited.remove(code);
        return new Result(false, 0, minBound);
    }

    private static int heuristic(int[][] positions, int keys, Map<Character, Point> keyPositions) {
        int totalDist = 0;
        for (Map.Entry<Character, Point> e : keyPositions.entrySet()) {
            char c = e.getKey();
            if ((keys & (1 << (c - 'a'))) != 0) continue;
            int minDist = Integer.MAX_VALUE;
            for (int[] pos : positions) {
                int dist = Math.abs(pos[0] - e.getValue().x) + Math.abs(pos[1] - e.getValue().y);
                if (dist < minDist) minDist = dist;
            }
            totalDist += minDist;
        }
        return totalDist;
    }


    private static List<Step> getReachableKeys(char[][] maze, int[] start, int keys) {
        int rows = maze.length;
        int cols = maze[0].length;
        boolean[][] visited = new boolean[rows][cols];
        Queue<int[]> queue = new ArrayDeque<>();
        List<Step> result = new ArrayList<>();

        queue.offer(new int[]{start[0], start[1], 0});
        visited[start[0]][start[1]] = true;

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int x = curr[0], y = curr[1], dist = curr[2];

            for (int[] dir : DIRS) {
                int nx = x + dir[0], ny = y + dir[1];
                if (nx < 0 || ny < 0 || nx >= rows || ny >= cols || visited[nx][ny]) continue;

                char c = maze[nx][ny];
                if (c == '#') continue;

                if (c >= 'A' && c <= 'Z' && ((keys >> (c - 'A')) & 1) == 0) continue; // дверь закрыта

                visited[nx][ny] = true;

                if (c >= 'a' && c <= 'z' && ((keys >> (c - 'a')) & 1) == 0) {
                    result.add(new Step(new Point(nx, ny), c, dist + 1));
                } else {
                    queue.offer(new int[]{nx, ny, dist + 1});
                }
            }
        }

        return result;


    }

    private static final int[][] DIRS = {
            {0, 1}, {1, 0}, {0, -1}, {-1, 0}
    };


    public static void main(String[] args) throws IOException {
        char[][] data = getInput();
        int result = solve(data);

        if (result == Integer.MAX_VALUE) {
            System.out.println("No solution found");
        } else {
            System.out.println(result);
        }
    }
}