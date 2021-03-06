package io.anuke.lsystem.evolution;

import io.anuke.ucore.lsystem.IEvaluator;
import io.anuke.ucore.lsystem.LTree;
import io.anuke.ucore.lsystem.LTree.Leaf;
import io.anuke.ucore.lsystem.LTree.Line;
import io.anuke.ucore.util.GridMap;

public enum Evaluator implements IEvaluator{
	leastIntersect {
		@Override
		public float getScore(LTree tree){
			GridMap<Integer> map = new GridMap<>();
			float cellsize = 3f;
			
			float surface = 0f;
			float min = 99999, max = 0;
			int volume = tree.lines.size;
			
			for(Line line : tree.lines){
				surface += Math.abs(line.x1 - line.x2);
				
				int cx = (int)(line.x1 / cellsize), cy = (int)(line.y1 / cellsize);
				int before = map.get(cx, cy) == null ? 0 : map.get(cx, cy);
				
				min = Math.min(line.x1, min);
				max = Math.max(line.x1, max);
				
				if(Math.abs(line.x1) > 100 || line.y1 > 100) return -1;
				
				map.put(cx, cy, before + 1);
				
				if(before > 1){
					surface -= (before - 1)/10f;
				}
				
				if(line.y1 < 0 || line.y2 < 0 || line.y1 > maxY || line.y2 > maxY){
					return -1;
				}
			}
			
			return surface*4f - volume - Math.abs(max - min);
		}
	},
	leafcount{
		@Override
		public float getScore(LTree tree){
			GridMap<Integer> map = new GridMap<>();
			float cellsize = 2f;
			
			float volume = tree.lines.size;
			float surfacearea = 0f;
			
			float xbounds = 200f;
			
			if(tree.lines.size <= 2){
				return -1;
			}
			
			for(Line line : tree.lines){
				//limits on going downwards, etc
				if(line.y1 < 0 || line.y2 < 0 || line.y1 > maxY || line.y2 > maxY ||
						line.x1 < -xbounds || line.x1 > xbounds){
					return -1;
				}
			}
			
			float maxleaf = Float.MIN_VALUE;
			float minleaf = Float.MAX_VALUE;
			
			for(Leaf leaf : tree.leaves) {
				if (leaf.x > maxleaf) {
					maxleaf = leaf.x;
				}else if(leaf.x < minleaf) {
					minleaf = leaf.x;
				}
				
				if(leaf.y < 20) {
					return volume += 1f;
				}
				
				int cx = (int)(leaf.x / cellsize), cy = (int)(leaf.y / cellsize);
				int before = map.get(cx, cy) == null ? 0 : map.get(cx, cy);
				
				map.put(cx, cy, before + 1);
			}
			if(tree.leaves.size != 0){
				surfacearea = maxleaf - minleaf;
			}
			
			return surfacearea + tree.leaves.size*400f - volume;
		}
	},
	symmetry{
		@Override
		public float getScore(LTree tree){
			GridMap<Integer> map = new GridMap<>();
			float cellsize = 2f;
			
			float surface = 0f;
			int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
			int height = Integer.MIN_VALUE;
			int volume = tree.lines.size;
			
			for(Line line : tree.lines){
				surface += 4f*(Math.abs(line.x1 - line.x2)*2f - Math.abs(line.y1 - line.y2));
				
				int cx = (int)((line.x1 + line.x2) / 2f / cellsize), cy = (int)((line.y1 + line.y2) / 2f / cellsize);
				int before = map.get(cx, cy) == null ? 0 : map.get(cx, cy);
				
				min = Math.min(cx, min);
				max = Math.max(cx, max);
				height = Math.max(cy, height);
				
				map.put(cx, cy, before + 1);
				
				if(line.y1 < 0 || line.y2 < 0 || line.y1 > maxY || line.y2 > maxY){
					return -1;
				}
			}
			
			if(min != -max){
				return -1;
			}
			
			for(Leaf leaf : tree.leaves) {
				if(leaf.y < 20) {
					surface -= 1f;
				}
			}
			
			
			for(int x = min; x <= max; x ++){
				for(int y = 0; y <= height; y ++){
					int value = (map.get(x, y) == null ? 0 : map.get(x, y));
					int valueM = (map.get(-x, y) == null ? 0 : map.get(-x, y));
					
					if(value != valueM){
						surface -= 10f;
					}
				}
			}
			
			
			return surface*5f + tree.leaves.size*15f - volume + height*4f;
		}
	},
	xSurface{
		@Override
		public float getScore(LTree tree){
			
			float surface = 0f;
			
			for(Line line : tree.lines){
				surface += Math.abs(line.x1 - line.x2);
				
				if(line.y1 < 0 || line.y2 < 0 || line.y1 > maxY || line.y2 > maxY){
					return -1;
				}
			}
			
			return surface*4f - tree.lines.size;
		}
	},
	ySurface{
		@Override
		public float getScore(LTree tree){
			
			float surface = 0f;
			
			for(Line line : tree.lines){
				surface += Math.abs(line.y1 - line.y2);
				
				if(line.y1 < 0 || line.y2 < 0 || line.y1 > maxY || line.y2 > maxY){
					return -1;
				}
			}
			
			return surface*4f - tree.lines.size;
		}
	};
	static float maxY = 300f;
	
	public abstract float getScore(LTree tree);
}
