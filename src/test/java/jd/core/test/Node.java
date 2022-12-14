package jd.core.test;

/*
 * https://docs.oracle.com/javase/tutorial/java/generics/bridgeMethods.html
 */
class Node<T> {

    public T data;

    public Node(T data) {
        this.data = data;
    }

    public void setData(T data) {
        System.out.println("Node.setData");
        this.data = data;
    }

    static class MyStaticNode extends Node<Integer> {
        public MyStaticNode(Integer data) {
            super(data);
        }

        public void setData(Integer data) {
            System.out.println("MyStaticNode.setData");
            super.setData(data);
        }

        class MySubNode extends MyStaticNode {

            public MySubNode(Integer data) {
                super(data);
            }
        }
    }

    class MyNode extends Node<Integer> {
        public MyNode(Integer data) {
            super(data);
        }

        public void setData(Integer data) {
            System.out.println("MyNode.setData");
            super.setData(data);
        }

        class MySubNode extends MyNode {

            public MySubNode(Integer data) {
                super(data);
            }
        }
    }
}
