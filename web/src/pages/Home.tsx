import { Typography } from "@material-tailwind/react";

export default function Home() {
    return (
        <div className="home">
            <Typography
                as="big"
                href="#"
                variant="h1"
                className="text-5xl mr-4 cursor-pointer py-1.5 font-normal">
                    WELCOME BACK.
            </Typography>
        </div>
    );
}