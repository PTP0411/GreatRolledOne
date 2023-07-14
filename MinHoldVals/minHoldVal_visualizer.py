import os
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt


def createMinHoldHeatmap(filename, fig_size=50, showfig=False):
    df = pd.read_csv(filename)
    trim_size = 100
    df = df.iloc[:trim_size, :trim_size]
    data = df.to_numpy()

    # Generate a custom diverging colormap
    plt.figure(figsize=(fig_size, fig_size))
    ax = plt.gca()

    # Plot the heatmap
    im = ax.imshow(data)

    # Create colorbar
    cbar = ax.figure.colorbar(im, ax=ax)
    cbar.ax.set_ylabel("Min hold values", rotation=-90, va="bottom")

    # Let the horizontal axes labeling appear on top.
    ax.tick_params(top=True, bottom=False, labeltop=True, labelbottom=False)

    # Rotate the tick labels and set their alignment.
    plt.setp(ax.get_xticklabels(), rotation=0, ha="right", rotation_mode="anchor")

    # set ticks
    ax.set_xticks(np.arange(0, data.shape[1], 5))
    ax.set_yticks(np.arange(0, data.shape[0], 5))
    ax.grid(which="minor", color="w", linestyle='-', linewidth=1)
    ax.tick_params(which="minor", bottom=False, left=False)

    # save fig
    filename = filename.split('.')[0]
    plt.savefig(f'{filename}.png')
    if showfig: plt.show()
    return im, cbar

if __name__ == '__main__':
    for filename in os.listdir('.'):
        if not filename.endswith('.txt'): continue
        print(filename)
        im, cbar = createMinHoldHeatmap(filename)