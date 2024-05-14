import java.io.*;
import java.nio.file.Files;
import java.util.*;


public class Graph {
	public static void main(String[] args) {
		Graph graph = new Graph();
		Scanner scanner = new Scanner(System.in);

		// ��ʾ�û������ļ���
		String filename;
		File file;
		do {
			System.out.print("�������ļ�������.txt��β����");
			filename = scanner.next();
			file = new File(filename);
			if (!file.exists()) {
				System.out.println("�ļ������ڣ����������롣");
			}
		} while (!file.exists());

		// ��������ͼ��չʾ
		graph.createGraph(filename);
		graph.showGraph();

		boolean exit = false;
		while (!exit) {
			// ��ʾ�˵�
			System.out.println("1. ��ѯ�ŽӴ�");
			System.out.println("2. �����ŽӴ��������ı�");
			System.out.println("3. ��������֮������·��");
			System.out.println("4. ����һ���ʵ����·��");
			System.out.println("5. �������");
			System.out.println("6. �˳�");
			System.out.print("��ѡ��Ҫִ�еĲ�����");

			// �û�ѡ�����
			String input = scanner.next();
			switch (input) {
				case "1":
					// ��ѯ�ŽӴ�
					System.out.print("�������������ʣ��Կո��������");
					String startWord = scanner.next();
					String endWord = scanner.next();
					graph.queryBridgeWords(startWord, endWord);
					break;
				case "2":
					// �����ŽӴ��������ı�
					System.out.print("������Ҫת�����ı���");
					scanner.nextLine(); // ������뻺����
					String inText = scanner.nextLine();
					System.out.println(graph.generateNewText(inText));
					break;
				case "3":
					// ������������֮������·��
					System.out.print("�������������ʣ��Կո��������");
					startWord = scanner.next();
					endWord = scanner.next();
					graph.calcShortestPath(startWord, endWord);
					break;
				case "4":
					// ����ĳ�����ʵ���һ���ʵ����·��
					System.out.print("��������ʼ���ʣ�");
					startWord = scanner.next();
					graph.calcShortestPath(startWord);
					break;
				case "5":
					// �������
					graph.randomWalk(scanner);
					break;
				case "6":
					// �˳�����
					exit = true;
					break;
				default:
					System.out.println("��Ч���롣");
					break;
			}
			if (exit) {
				System.exit(0);
				;
			}
		}
	}
	private static final int DEFAULT_ARRAY_SIZE = 1000; // Ĭ�������С
	private List<String> words;  // �����б�
	private Map<String, Integer> wordMap;  // ������ӳ��Ϊ���
	private int[][] weight;  // ��ʾͼ�Ķ�ά���󣬴洢�ߵ�Ȩ��
	private boolean[][] shortest;  // ��ʾ���·���ľ��󣬱��ڽ�·��������ʾ

	/**
	 * ��ָ���ļ��ж�ȡ���ݣ����������ݹ�������ͼ��
	 * @param filename Ҫ��ȡ���ļ�·��
	 * @return �������ͼ�ɹ�����true�����򷵻�false
	 */
	private boolean createGraph(String filename) {
		File inputFile = new File(filename);
		try (BufferedReader bReader = new BufferedReader(new InputStreamReader(Files.newInputStream(inputFile.toPath())))) {
			initialize(); // ��ʼ��ͼ�����ݽṹ
			Fileprocess(bReader); // �����ļ����ݣ���������ͼ
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * ��ʼ��ͼ�����ݽṹ�����������б�����ӳ�䡢�ߵ�Ȩ�ؾ����·����Ǿ���
	 */
	private void initialize() {
		words = new ArrayList<>();
		wordMap = new HashMap<>();
		weight = new int[DEFAULT_ARRAY_SIZE][DEFAULT_ARRAY_SIZE];
		shortest = new boolean[DEFAULT_ARRAY_SIZE][DEFAULT_ARRAY_SIZE];
	}

	/**
	 * �����ļ����ݣ���������ͼ��
	 * @param bReader ���ڶ�ȡ�ļ����ݵ�BufferedReader
	 * @throws IOException �����ȡ�ļ�ʱ�����������׳� IOException
	 */
	private void Fileprocess(BufferedReader bReader) throws IOException {
		List<String> tempWordList = new ArrayList<>();  // ��ʱ�������е��ʣ��Ӷ������ߺ�Ȩ��
		while (bReader.ready()) {
			// ���ж��룬ȥ������ĸ�ַ���������ת��ΪСд
			String line = bReader.readLine().replaceAll("[^ a-zA-Z,.?!:;\"]+", "").toLowerCase();
			String[] lineArray = line.split("\\W+");  // ʹ�÷ǵ����ַ����зָ�
			for (String word : lineArray) {
				if (!word.isEmpty()) {  // �ǿյ��ʴ���
					tempWordList.add(word);
					wordMap.putIfAbsent(word, words.size());  // �������δ���ֹ�������ӵ�wordMap��words��
					if (!words.contains(word)) {
						words.add(word);
					}
				}
			}
		}
		// ��������ͼ��ͳ�Ʊߵ�Ȩ��
		for (int i = 1; i < tempWordList.size(); i++) {
			String startWord = tempWordList.get(i - 1);
			String endWord = tempWordList.get(i);
			weight[wordMap.get(startWord)][wordMap.get(endWord)]++;  // Ȩ��+1
		}
	}

	/**
	 * ��ѯ��������֮����ŽӴ�
	 * @param startWord ��ʼ����
	 * @param endWord ��������
	 * @return �����ŽӴ��б���������ڻ������뵥�ʲ��ڴʵ����򷵻�null
	 */
	public List<String> queryBridgeWords(String startWord, String endWord) {
		// �������뵥�ʣ�ȷ����������Ч��
		startWord = cleanWord(startWord);
		endWord = cleanWord(endWord);

		// �����ʼ���ʺͽ��������Ƿ��ڴʵ���
		if (!wordMap.containsKey(startWord) || !wordMap.containsKey(endWord)) {
			return null;
		}

		// �����ŽӴʵ��б�
		List<String> bridgeWords = new ArrayList<>();
		// ��ȡ��ʼ���ʺͽ������ʵ�����
		int startIndex = wordMap.get(startWord);
		int endIndex = wordMap.get(endWord);

		// ������ʼ���ʵ������ڽӵ㣬Ѱ���ŽӴ�
		for (int i = 0; i < weight[startIndex].length; i++) {
			if (weight[startIndex][i] > 0 && weight[i][endIndex] > 0) {
				bridgeWords.add(words.get(i));
				System.out.println("�ҵ����ŽӴ�: " + bridgeWords);
			}
		}
		if (bridgeWords.isEmpty()) {
			System.out.println("û���ҵ��ŽӴ�");
		}
		// �����ŽӴ��б�
		return bridgeWords;
	}

	/**
	 * �������ı������ݾ��ı����ŽӴ�
	 * @param oldText ���ı�
	 * @return �������ı�
	 */
	public String generateNewText(String oldText) {
		// ����һ���µ�StringBuilder���������ı�
		StringBuilder newText = new StringBuilder();
		// ������ı���ȷ��������Ч��
		String cleanedText = cleanText(oldText);
		// ���������ı����ո�ָ�ɵ�������
		String[] wordArray = cleanedText.toLowerCase().split(" ");

		// ����һ�������������
		Random random = new Random();
		// ��ʼ����ʼ����
		String start = wordArray[0];
		// ����ʼ������ӵ����ı���
		newText.append(start).append(" ");

		// ����ÿ�����ʣ��������ı�
		for (int i = 1; i < wordArray.length; i++) {
			// ��ȡ��ǰ������Ϊ��������
			String end = wordArray[i];
			// ��ѯ��ʼ���ʺͽ�������֮����ŽӴ�
			List<String> bridgeWords = queryBridgeWords(start, end);

			// ��������ŽӴʣ������ѡ��һ����ӵ����ı���
			if (!bridgeWords.isEmpty()) {
				int randomIndex = random.nextInt(bridgeWords.size());
				newText.append(bridgeWords.get(randomIndex)).append(" ");
			}

			// ������������ӵ����ı��У�������ʼ���ʸ���Ϊ��ǰ��������
			newText.append(end).append(" ");
			start = end;
		}

		// �������ɵ����ı�
		return newText.toString().trim();
	}

	/**
	 * �����ı���ȥ������ĸ�ַ�
	 * @param text ��������ı�
	 * @return �����������ı�
	 */
	private String cleanText(String text) {
		return text.replaceAll("[^ a-zA-Z,.?!:;\"]+", "").trim();
	}

	/**
	 * �����ʣ�ȥ������ĸ�ַ�
	 * @param word ������ĵ���
	 * @return ���������ĵ���
	 */
	private String cleanWord(String word) {
		return word.replaceAll("[^ a-zA-Z,.?!:;\"]+", "");
	}


	/**
	 * Dijkstra����������������·��
	 * @param v0  ���
	 * @param v1 �յ�
	 */
	public void Dijkstra(int v0, int v1, String label) {
		int N = words.size();
		int[] path = new int[N];
		int[] dist = new int[N];
		boolean[] visited = new boolean[N];

		// ��ʼ��·������Ϊ���ֵ
		for (int i = 0; i < N; i++) {
			dist[i] = DEFAULT_ARRAY_SIZE;
			visited[i] = false;
		}

		// ʹ�����ȶ��д洢δ���ʵĽڵ㣬����������
		PriorityQueue<Integer> pq = new PriorityQueue<>((a, b) -> dist[a] - dist[b]);

		dist[v0] = 0;
		pq.offer(v0);

		while (!pq.isEmpty()) {
			int u = pq.poll();
			if (visited[u]) continue;
			visited[u] = true;

			// ������ڵ�u���ڽڵ�ľ���
			for (int v = 0; v < N; v++) {
				if (!visited[v] && weight[u][v] != 0 && dist[u] + weight[u][v] < dist[v]) {
					dist[v] = dist[u] + weight[u][v];
					path[v] = u;
					pq.offer(v);
				}
			}
		}

		// ������·��
		if (dist[v1] != DEFAULT_ARRAY_SIZE) {
			System.out.println(words.get(v0) + " -> " + words.get(v1) + " ���·���ĳ�Ϊ" + dist[v1]);
			Stack<Integer> s = new Stack<Integer>();
			int u = v1;
			while (u != v0) {  // ��·��ѹջ
				s.push(u);
				int v = u;
				u = path[u];
				shortest[u][v] = true;
			}
			s.push(v0);
			while (!s.empty()) {
				System.out.print(words.get(s.pop()));
				if (!s.empty()) {
					System.out.print(" -> ");
				}
			}
			System.out.println("\n");
			String dotFormat = getAllPath();
			createDotGraph(dotFormat, "DGraph" + label);
		} else {
			System.out.println( " ���ɴ�\n");
		}
	}

	/**
	 * ������ߣ��������ʼ�������ɵ��ı������ո�ֹͣ����
	 * @param cin ���ڶ�ȡ�����Scanner����
	 */
	public void randomWalk(Scanner cin) {
		Random random = new Random();
		FileWriter writer = null;

		try {
			writer = new FileWriter("traversal_path.txt"); // �����ļ�д����

			// ��ͼ�����ѡ����ʼ�ڵ�
			String currentWord = words.get(random.nextInt(words.size()));

			// ��ʼ�������
			traverse(currentWord, new ArrayList<>(), writer, cin, random);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.out.println("\n�������");
	}

	// �ݹ麯����ʵ���������
	private void traverse(String currentWord, List<String> visitedWords, FileWriter writer, Scanner cin, Random random) throws IOException {
		// �����ǰ�ڵ�
		System.out.print(currentWord + " ");
		writer.write(currentWord + " ");

		// ����ǰ�ڵ���ӵ��ѷ��ʽڵ��б���
		visitedWords.add(currentWord);

		// ��ȡ��ǰ�ڵ�������ڽӵ���
		List<String> lin = getlin(currentWord);

		// ���ڽӵ��������ѡ��һ�����м�������
		List<String> neigh = new ArrayList<>(lin);
		neigh.removeAll(visitedWords);

		// ���û��δ���ʵ��ڽӵ��ʣ����������
		if (neigh.isEmpty()) {
			return;
		}

		// ��δ���ʵ��ڽӵ��������ѡ��һ�����м�������
		String nextWord = neigh.get(random.nextInt(neigh.size()));

		// �ȴ��û�����
		String input = cin.nextLine();
		if (input.equals("s")) {
			return; // �û�ֹͣ����
		}

		// �ݹ���ü�������
		traverse(nextWord, visitedWords, writer, cin, random);
	}

	// ��ȡָ�����ʵ������ڽӵ���
	private List<String> getlin(String word) {
		int index = wordMap.get(word);
		List<String> lin = new ArrayList<>();
		for (int i = 0; i < weight[index].length; i++) {
			if (weight[index][i] != 0 && weight[index][i] != DEFAULT_ARRAY_SIZE) {
				lin.add(words.get(i));
			}
		}
		return lin;
	}
	/**
	 * ����APIд�ģ�����
	 * @param dotFormat ͼ������·�����ַ���
	 * @param fileName ���ɵ�ͼ�ļ�
	 */
	public static void createDotGraph(String dotFormat,String fileName)
	{
		GraphViz g=new GraphViz();
		g.addln(g.start_graph());
		g.add(dotFormat);
		g.addln(g.end_graph());
		String type = "jpg";
		g.decreaseDpi();
		g.decreaseDpi();
		File out = new File(fileName+"."+ type);
		g.writeGraphToFile( g.getGraph( g.getDotSource(), type ), out );
	}


	/**
	 *
	 * @return ����dot�﷨��ʽ���ַ�����ʾͼ�����б�
	 */
	public String getAllPath() {
		StringBuilder paths = new StringBuilder();
		for (int i = 0; i < words.size(); i++) {
			for (int j = 0; j < words.size(); j++) {
				if (weight[i][j] != 0 && weight[i][j] != DEFAULT_ARRAY_SIZE) {
					paths.append(words.get(i)).append("->").append(words.get(j));
					paths.append("[label=\"").append(weight[i][j]).append("\"");
					if (shortest[i][j]) {
						paths.append(", color=\"red\"");
					}
					paths.append("];");
				}
			}
		}
		return paths.toString();
	}


	/**
	 * ����ͼ�ļ�
	 */
	void showGraph(){
		String dotFormat = getAllPath();
		createDotGraph(dotFormat, "DGraph");
		new show("DGraph.jpg");
	}

	/**
	 * ������������֮������·������չʾ������
	 * @param word1 ��һ������
	 * @param word2 �ڶ�������
	 */
	public void calcShortestPath(String word1, String word2) {
		// ��������ĵ��ʣ�ȷ����������Ч��
		word1 = cleanWord(word1);
		word2 = cleanWord(word2);

		// ��鵥���Ƿ������ͼ��
		if (!words.contains(word1) || !words.contains(word2)) {
			System.out.println("No " + word1 + " or " + word2 + " in the graph!");
			return;
		}

		// ������������֮������·������չʾ���
		Dijkstra(wordMap.get(word1), wordMap.get(word2), "Calc");
		new show("DGraphCalc.jpg");
	}


	/**
	 * ĳ���ʵ����ⵥ�ʼ�����·������չʾ������
	 * @param word ��ʼ����
	 */
	void calcShortestPath(String word) {
		// ��������ĵ��ʣ�ȷ��������Ч��
		word = cleanWord(word);

		// ��鵥���Ƿ������ͼ��
		if (!words.contains(word)) {
			System.out.println("No " + word + " in the graph!");
			return;
		}

		// �������Ŀ�굥������
		Random random = new Random();
		int rand = random.nextInt(words.size());

		// ������ʼ���ʵ�ÿ������֮������·������չʾ���
		for (int i = 0; i < words.size(); i++) {
			Dijkstra(wordMap.get(word), i, word + "To" + words.get(i));
			if (i == rand) {
				new show("DGraph" + word + "To" + words.get(i) + ".jpg");
			}
		}
	}
	}



