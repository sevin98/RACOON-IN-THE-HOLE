import { Scene } from 'phaser';

export class MainMenu extends Scene
{
    constructor ()
    {
        super('MainMenu');
    }

    preload(){

    }
    create ()
    {

        this.scene.start("game");
    }
}

export default MainMenu;
