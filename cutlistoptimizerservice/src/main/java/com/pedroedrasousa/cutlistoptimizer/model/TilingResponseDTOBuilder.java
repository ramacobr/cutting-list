package com.pedroedrasousa.cutlistoptimizer.model;

import java.util.List;

public class TilingResponseDTOBuilder {

    private Solution solution;

    private String info;

    public Solution getSolution() {
        return solution;
    }

    public TilingResponseDTOBuilder setSolutions(Solution solution) {
        this.solution = solution;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public TilingResponseDTOBuilder setInfo(String info) {
        this.info = info;
        return this;
    }

    public TillingResponseDTO build() {

        if (solution == null) {
            return null;
        }

        TillingResponseDTO tillingResponseDTO = new TillingResponseDTO();

        for (Mosaic tileNode : solution.getMosaics()) {
            TillingResponseDTO.Mosaic mosaic = new TillingResponseDTO.Mosaic();
            mosaic.setUsedArea(tileNode.getRootTileNode().getUsedArea());
            mosaic.setUsedAreaRatio(tileNode.getRootTileNode().getUsedAreaRatio());
            mosaic.setNbrHorizontal(tileNode.getRootTileNode().getNbrFinalHorizontal());
            mosaic.setNbrVertical(tileNode.getRootTileNode().getNbrFinalVertical());
            mosaic.setCuts(tileNode.getCuts());
            mosaic.setNbrWasted(tileNode.getRootTileNode().getNbrUnusedTiles());
            mosaic.setHvRatio(tileNode.getHVDiff());
            mosaic.setBase(new TillingResponseDTO.Tile(tileNode.getRootTileNode()));
            mosaic.setUnusedArea(tileNode.getUnusedArea());
            addChildrenToList(tileNode.getRootTileNode(), mosaic.getTiles());
            tillingResponseDTO.getMosaics().add(mosaic);
        }

        for (TileDimensions tileDimension : solution.getNoFitTiles()) {
            tillingResponseDTO.addNoFitTile(tileDimension);
        }

        tillingResponseDTO.setUnusedArea(solution.getUnusedArea());
        tillingResponseDTO.setHvRatio(solution.getHVDiff());
        tillingResponseDTO.setReturnCode("0");
        tillingResponseDTO.setElapsedTime(solution.getElapsedTime());

        return tillingResponseDTO;
    }

    /**
     * Recursively adds all the tiles contained in the specified node the the provided list.
     *
     * @param tileNode Node to traverse.
     * @param dtoList List to append child nodes.
     */
    private static void addChildrenToList(TileNode tileNode, List<TillingResponseDTO.Tile> dtoList) {
        TillingResponseDTO.Tile tileDto = new TillingResponseDTO.Tile(tileNode);
        dtoList.add(tileDto);
        if (tileNode.hasChildren()) {
            tileDto.setHasChildren(true);
            if (tileNode.getChild1() != null) {
                addChildrenToList(tileNode.getChild1(), dtoList);
            }
            if (tileNode.getChild2() != null) {
                addChildrenToList(tileNode.getChild2(), dtoList);
            }
        } else {
            tileDto.setHasChildren(false);
        }
    }
}
