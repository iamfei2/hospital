package com.hospit.algorithm;

import java.io.Serializable;
import java.util.*;

/**
 * 孤立森林算法实现 - 多维异常检测
 * 核心思想：异常点容易被随机切分出来（路径短），正常点需要更多次切分
 * 使用多棵孤立树组成的森林进行异常评分
 */
public class IsolationForest {

    private int numTrees = 100;      // 树的数量
    private int sampleSize = 256;    // 随机采样大小
    private int maxDepth = 10;       // 最大深度
    private double contamination = 0.1;  // 污染率（预期异常比例）

    private List<IsolationTree> trees;   // 孤立森林
    private transient Random random;

    public int getNumTrees() { return numTrees; }
    public void setNumTrees(int numTrees) { this.numTrees = numTrees; }
    public int getSampleSize() { return sampleSize; }
    public void setSampleSize(int sampleSize) { this.sampleSize = sampleSize; }
    public int getMaxDepth() { return maxDepth; }
    public void setMaxDepth(int maxDepth) { this.maxDepth = maxDepth; }
    public double getContamination() { return contamination; }
    public void setContamination(double contamination) { this.contamination = contamination; }
    public List<IsolationTree> getTrees() { return trees; }
    public void setTrees(List<IsolationTree> trees) { this.trees = trees; }
    public Random getRandom() { return random; }
    public void setRandom(Random random) { this.random = random; }

    public IsolationForest() {
        this.random = new Random();
    }

    public IsolationForest(int numTrees, int sampleSize, int maxDepth, double contamination) {
        this.numTrees = numTrees;
        this.sampleSize = sampleSize;
        this.maxDepth = maxDepth;
        this.contamination = contamination;
        this.random = new Random();
        this.trees = new ArrayList<>();
    }

    // 构建孤立森林
    public void buildForest(List<double[]> data) {
        trees = new ArrayList<>();
        
        int effectiveSampleSize = Math.min(sampleSize, data.size());
        
        for (int i = 0; i < numTrees; i++) {
            List<double[]> sample = sampleWithReplacement(data, effectiveSampleSize);
            IsolationTree tree = new IsolationTree(maxDepth);
            tree.build(sample, random);
            trees.add(tree);
        }
    }

    // 预测单个实例的异常分数
    public double predict(double[] instance) {
        if (trees == null || trees.isEmpty()) {
            return 0.0;
        }
        
        double sumPathLength = 0.0;
        for (IsolationTree tree : trees) {
            double pathLength = tree.pathLength(instance, 0, tree.getRoot());
            sumPathLength += pathLength;
        }
        
        double avgPathLength = sumPathLength / trees.size();
        double c = cValue(Math.max(1, sampleSize));
        
        double score = Math.pow(2, -avgPathLength / c);
        return score;
    }

    // 批量预测异常分数
    public double[] predictBatch(List<double[]> instances) {
        double[] scores = new double[instances.size()];
        for (int i = 0; i < instances.size(); i++) {
            scores[i] = predict(instances.get(i));
        }
        return scores;
    }

    // 有放回采样
    private List<double[]> sampleWithReplacement(List<double[]> data, int size) {
        List<double[]> sample = new ArrayList<>(size);
        int dataSize = data.size();
        for (int i = 0; i < size; i++) {
            int index = random.nextInt(dataSize);
            sample.add(data.get(index));
        }
        return sample;
    }

    // 计算调和常数c(n)
    private static double cValue(int n) {
        if (n <= 1) {
            return 0.0;
        }
        return 2.0 * (Math.log(n - 1) + 0.5772156649) - (2.0 * (n - 1) / (double) n);
    }

    // 获取模型参数
    public Map<String, Object> getModelParams() {
        Map<String, Object> params = new HashMap<>();
        params.put("numTrees", numTrees);
        params.put("sampleSize", sampleSize);
        params.put("maxDepth", maxDepth);
        params.put("contamination", contamination);
        
        List<Map<String, Object>> treeParams = new ArrayList<>();
        for (IsolationTree tree : trees) {
            treeParams.add(tree.getTreeParams());
        }
        params.put("trees", treeParams);
        return params;
    }

    public void loadFromParams(Map<String, Object> params) {
        this.numTrees = (Integer) params.getOrDefault("numTrees", 100);
        this.sampleSize = (Integer) params.getOrDefault("sampleSize", 256);
        this.maxDepth = (Integer) params.getOrDefault("maxDepth", 10);
        this.contamination = ((Number) params.getOrDefault("contamination", 0.1)).doubleValue();
        
        List<Map<String, Object>> treeParamsList = (List<Map<String, Object>>) params.get("trees");
        if (treeParamsList != null) {
            this.trees = new ArrayList<>();
            for (Map<String, Object> treeParams : treeParamsList) {
                IsolationTree tree = new IsolationTree(maxDepth);
                tree.loadFromParams(treeParams);
                this.trees.add(tree);
            }
        }
    }

    public static class IsolationTree implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private int maxDepth;
        private TreeNode root;
        private transient Random random;

        public int getMaxDepth() { return maxDepth; }
        public void setMaxDepth(int maxDepth) { this.maxDepth = maxDepth; }
        public TreeNode getRoot() { return root; }
        public void setRoot(TreeNode root) { this.root = root; }
        public Random getRandom() { return random; }
        public void setRandom(Random random) { this.random = random; }

        public IsolationTree(int maxDepth) {
            this.maxDepth = maxDepth;
            this.random = new Random();
        }

        // 构建孤立树
        public void build(List<double[]> data, Random random) {
            this.random = random;
            if (data == null || data.isEmpty() || data.get(0).length == 0) {
                return;
            }
            this.root = buildTree(data, 0, random);
        }

        // 递归构建树
        private TreeNode buildTree(List<double[]> data, int currentDepth, Random random) {
            if (data.isEmpty() || currentDepth >= maxDepth || data.size() <= 1) {
                return new TreeNode(null, null, null, data.size(), true);
            }

            int featureCount = data.get(0).length;
            int featureIndex = random.nextInt(featureCount);
            
            double minVal = Double.MAX_VALUE;
            double maxVal = Double.MIN_VALUE;
            for (double[] row : data) {
                minVal = Math.min(minVal, row[featureIndex]);
                maxVal = Math.max(maxVal, row[featureIndex]);
            }

            double splitValue;
            if (minVal == maxVal) {
                return new TreeNode(null, null, null, data.size(), true);
            }
            
            splitValue = minVal + random.nextDouble() * (maxVal - minVal);

            List<double[]> leftData = new ArrayList<>();
            List<double[]> rightData = new ArrayList<>();
            for (double[] row : data) {
                if (row[featureIndex] < splitValue) {
                    leftData.add(row);
                } else {
                    rightData.add(row);
                }
            }

            TreeNode left = buildTree(leftData, currentDepth + 1, random);
            TreeNode right = buildTree(rightData, currentDepth + 1, random);

            return new TreeNode(featureIndex, splitValue, null, data.size(), false, left, right);
        }

        // 计算实例的路径长度
        public double pathLength(double[] instance, int currentDepth, TreeNode node) {
            if (node == null || node.isLeaf()) {
                return currentDepth + cValue(node != null ? node.getSize() : 1);
            }

            if (node.getFeatureIndex() < 0 || node.getFeatureIndex() >= instance.length) {
                return currentDepth + cValue(node.getSize());
            }

            double featureValue = instance[node.getFeatureIndex()];
            if (featureValue < node.getSplitValue()) {
                return pathLength(instance, currentDepth + 1, node.getLeft());
            } else {
                return pathLength(instance, currentDepth + 1, node.getRight());
            }
        }

        // 获取树参数
        public Map<String, Object> getTreeParams() {
            Map<String, Object> params = new HashMap<>();
            params.put("maxDepth", maxDepth);
            params.put("root", root != null ? root.toMap() : null);
            return params;
        }

    // 从参数加载模型
    public void loadFromParams(Map<String, Object> params) {
            this.maxDepth = (Integer) params.getOrDefault("maxDepth", 10);
            Map<String, Object> rootParams = (Map<String, Object>) params.get("root");
            if (rootParams != null) {
                this.root = TreeNode.fromMap(rootParams);
            }
        }
    }

    public static class TreeNode implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private int featureIndex;
        private double splitValue;
        private String featureName;
        private int size;
        private boolean isLeaf;
        private TreeNode left;
        private TreeNode right;

        public int getFeatureIndex() { return featureIndex; }
        public void setFeatureIndex(int featureIndex) { this.featureIndex = featureIndex; }
        public double getSplitValue() { return splitValue; }
        public void setSplitValue(double splitValue) { this.splitValue = splitValue; }
        public String getFeatureName() { return featureName; }
        public void setFeatureName(String featureName) { this.featureName = featureName; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
        public boolean isLeaf() { return isLeaf || (left == null && right == null); }
        public void setLeaf(boolean leaf) { isLeaf = leaf; }
        public TreeNode getLeft() { return left; }
        public void setLeft(TreeNode left) { this.left = left; }
        public TreeNode getRight() { return right; }
        public void setRight(TreeNode right) { this.right = right; }

        public TreeNode(Integer featureIndex, Double splitValue, String featureName, int size, boolean isLeaf) {
            this.featureIndex = featureIndex != null ? featureIndex : -1;
            this.splitValue = splitValue != null ? splitValue : 0.0;
            this.featureName = featureName;
            this.size = size;
            this.isLeaf = isLeaf;
        }

        public TreeNode(int featureIndex, double splitValue, String featureName, int size, boolean isLeaf, 
                       TreeNode left, TreeNode right) {
            this(featureIndex, splitValue, featureName, size, isLeaf);
            this.left = left;
            this.right = right;
        }

        // 转换为Map
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("featureIndex", featureIndex);
            map.put("splitValue", splitValue);
            map.put("featureName", featureName);
            map.put("size", size);
            map.put("isLeaf", isLeaf);
            if (left != null) map.put("left", left.toMap());
            if (right != null) map.put("right", right.toMap());
            return map;
        }

        // 从Map创建节点
        public static TreeNode fromMap(Map<String, Object> map) {
            if (map == null) return null;
            
            TreeNode node = new TreeNode(
                (Integer) map.get("featureIndex"),
                (Double) map.get("splitValue"),
                (String) map.get("featureName"),
                (Integer) map.get("size"),
                (Boolean) map.get("isLeaf")
            );
            
            if (map.containsKey("left")) {
                node.setLeft(fromMap((Map<String, Object>) map.get("left")));
            }
            if (map.containsKey("right")) {
                node.setRight(fromMap((Map<String, Object>) map.get("right")));
            }
            
            return node;
        }
    }
}
