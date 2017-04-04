package com.pedroedrasousa.tiling.model;

import java.util.List;

public class TillingRequestDTO {

    private List<TileInfoDTO> baseTiles;

    private List<TileInfoDTO> tiles;

    private Configuration configuration;

    public List<TileInfoDTO> getBaseTiles() {
        return baseTiles;
    }

    public void setBaseTiles(List<TileInfoDTO> baseTiles) {
        this.baseTiles = baseTiles;
    }

    public List<TileInfoDTO> getTiles() {
        return tiles;
    }

    public void setTiles(List<TileInfoDTO> tiles) {
        this.tiles = tiles;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public static class TileInfoDTO {

        private int id;

        private int width;

        private int height;

        private int count;

        private boolean enabled;

        public TileInfoDTO() {}

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
